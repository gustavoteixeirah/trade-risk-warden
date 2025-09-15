package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.ports.input.EnforceBreachInputPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.DecisionType;
import com.teixeirah.trw.domain.trading.TradingPort;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccountRepository;

import java.time.Instant;
import java.util.Map;

public class EnforceBreachUseCase implements EnforceBreachInputPort {

    private final TradingPort trading;
    private final UserAccountRepository users;
    private final Notifier notifier;

    public EnforceBreachUseCase(TradingPort trading, UserAccountRepository users, Notifier notifier) {
        this.trading = trading;
        this.users = users;
        this.notifier = notifier;
    }

    @Override
    public void handle(ClientId clientId, DecisionType type, Money loss, Money limit, Instant at) {
        final var user = users.find(clientId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + clientId.value()));

        trading.cancelOpenOrders(user.clientId());
        trading.closeAllAtMarket(user.clientId());

        var newState = switch (type) {
            case DAILY_BREACH -> user.state().withDailyBlock(at);
            case MAX_BREACH -> user.state().withPermanentBlock(at);
            default -> throw new IllegalArgumentException("Unexpected decision: " + type);
        };
        var updated = new com.teixeirah.trw.domain.user.UserAccount(
                user.clientId(), user.limits(), user.initialBalance(), user.tz(), user.apiKey(), user.apiSecret(), newState
        );
        users.save(updated);

        notifier.publish(new Event(
                clientId,
                at,
                type == DecisionType.MAX_BREACH ? EventType.MAX_RISK_TRIGGERED : EventType.DAILY_RISK_TRIGGERED,
                Map.of("loss", loss.amount(), "limit", limit.amount(), "currency", limit.currency().getCurrencyCode())
        ));
    }
}
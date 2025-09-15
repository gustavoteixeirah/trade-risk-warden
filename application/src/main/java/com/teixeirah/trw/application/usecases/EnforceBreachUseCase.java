package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.ports.input.EnforceBreachInputPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.DecisionType;
import com.teixeirah.trw.domain.trading.TradingPort;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.RiskState;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;

import java.time.Instant;
import java.time.ZoneId;
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

        if (shouldShortCircuit(type, user, at)) return;

        trading.flattenAll(user.apiKey(), user.apiSecret());

        var newState = getNewState(type, at, user);
        var updated = new UserAccount(
                user.clientId(), user.limits(), user.initialBalance(), user.tz(), user.apiKey(), user.apiSecret(), newState);
        users.save(updated);

        notifier.publish(new Event(
                clientId,
                at,
                type == DecisionType.MAX_BREACH ? EventType.MAX_RISK_BREACH_ACTION_APPLIED : EventType.DAILY_RISK_BREACH_ACTION_APPLIED,
                Map.of(
                        "loss", loss.amount(),
                        "limit", limit.amount(),
                        "currency", limit.currency().getCurrencyCode(),
                        "snapshotTs", at,
                        "action", "flattenAll"
                )
        ));
    }

    private static RiskState getNewState(DecisionType type, Instant at, UserAccount user) {
        return switch (type) {
            case DAILY_BREACH -> user.state().withDailyBlock(at);
            case MAX_BREACH -> user.state().withPermanentBlock(at);
            default -> throw new IllegalArgumentException("Unexpected decision: " + type);
        };
    }

    private static boolean shouldShortCircuit(DecisionType type, UserAccount user, Instant at) {
        return switch (type) {
            case NONE -> true;
            case MAX_BREACH -> user.state().permanentBlocked();
            case DAILY_BREACH -> user.state().dailyBlocked()
                    && isSameTradingDay(user.state().dailyBlockedAt(), at, user.tz());
        };
    }

    private static boolean isSameTradingDay(Instant a, Instant b, ZoneId tz) {
        if (a == null || b == null) return false;
        return a.atZone(tz).toLocalDate().equals(b.atZone(tz).toLocalDate());
    }
}
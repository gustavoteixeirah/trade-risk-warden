package com.teixeirah.trw.infra.primary.listeners;


import com.teixeirah.trw.application.ports.input.EnforceBreachInputPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.risk.DecisionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

@Component
@RequiredArgsConstructor
class BreachEnforcementListener {

    private final EnforceBreachInputPort enforce;

    @EventListener(condition =
            "#e.type == T(com.teixeirah.trw.domain.notification.EventType).DAILY_RISK_TRIGGERED || " +
                    "#e.type == T(com.teixeirah.trw.domain.notification.EventType).MAX_RISK_TRIGGERED")
    void onBreach(Event e) {
        DecisionType decision =
                (e.type() == EventType.MAX_RISK_TRIGGERED) ? DecisionType.MAX_BREACH : DecisionType.DAILY_BREACH;

        var loss = money(e.details(), "loss", e.details().get("currency"));
        var limit = money(e.details(), "limit", e.details().get("currency"));
        Instant at = e.ts();

        enforce.handle(e.clientId(), decision, loss, limit, at);
    }

    private Money money(Map<String, Object> d, String key, Object currencyCode) {
        var amt = new BigDecimal(String.valueOf(d.get(key)));
        var cur = Currency.getInstance(String.valueOf(currencyCode));
        return new Money(amt, cur);
    }
}
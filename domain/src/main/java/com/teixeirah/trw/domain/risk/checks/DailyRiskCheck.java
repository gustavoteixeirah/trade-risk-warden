package com.teixeirah.trw.domain.risk.checks;


import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.risk.RiskCheck;
import com.teixeirah.trw.domain.risk.RiskDecision;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.UserAccount;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

public final class DailyRiskCheck implements RiskCheck {

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Optional<RiskDecision> evaluate(UserAccount user, PnlSnapshot snapshot) {
        var initial = user.initialBalance().value();
        var limits = user.limits();
        var currency = initial.currency();

        var realized = safe(snapshot.realizedPnlToday().amount());
        var dailyLoss = realized.signum() < 0 ? realized.negate() : ZERO;

        var dailyLimitAmt = resolve(limits.daily(), initial.amount());
        if (gte(dailyLoss, dailyLimitAmt)) {
            return Optional.of(RiskDecision.daily(
                    new Money(dailyLoss, currency),
                    new Money(dailyLimitAmt, currency)
            ));
        }
        return Optional.empty();
    }

    static BigDecimal resolve(RiskThreshold t, BigDecimal initial) {
        if (t == null || t.value() == null) return ZERO;
        return (t.type() == ThresholdType.ABSOLUTE) ? t.value()
                : initial.multiply(t.value()).movePointLeft(2);
    }

    static BigDecimal safe(BigDecimal v) {
        return v == null ? ZERO : v;
    }

    static boolean gte(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) >= 0;
    }
}
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

public final class MaxRiskCheck implements RiskCheck {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public Optional<RiskDecision> evaluate(UserAccount user, PnlSnapshot snapshot) {
        var initial = user.initialBalance().value();                 // Money
        var limits = user.limits();
        var currency = initial.currency();

        var cumulative = safe(snapshot.cumulativePnl().amount());
        var totalLoss = cumulative.signum() < 0 ? cumulative.negate() : ZERO;

        var maxLimitAmt = resolve(limits.max(), initial.amount());
        if (gte(totalLoss, maxLimitAmt)) {
            // ❌ your broken line had BigDecimal; ✅ wrap as Money and use TOTAL loss vs MAX limit
            return Optional.of(RiskDecision.max(
                    new Money(totalLoss, currency),
                    new Money(maxLimitAmt, currency)
            ));
        }
        return Optional.empty();
    }

    static BigDecimal resolve(RiskThreshold t, BigDecimal initial) {
        if (t == null || t.value() == null) return ZERO;
        return (t.type() == ThresholdType.ABSOLUTE) ? t.value()
                : initial.multiply(t.value()).movePointLeft(2); // percentage
    }

    static BigDecimal safe(BigDecimal v) {
        return v == null ? ZERO : v;
    }

    static boolean gte(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) >= 0;
    }
}
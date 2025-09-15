package com.teixeirah.trw.domain.risk;


import com.teixeirah.trw.domain.money.Money;


public record RiskDecision(DecisionType type, Money loss, Money limit) {

    public static RiskDecision daily(Money loss, Money limit) {
        return new RiskDecision(DecisionType.DAILY_BREACH, loss, limit);
    }

    public static RiskDecision max(Money loss, Money limit) {
        return new RiskDecision(DecisionType.MAX_BREACH, loss, limit);
    }
}



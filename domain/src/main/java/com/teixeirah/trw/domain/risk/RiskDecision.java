package com.teixeirah.trw.domain.risk;

import com.teixeirah.trw.domain.money.Money;

public record RiskDecision(DecisionType type, Money loss, Money limit) {}



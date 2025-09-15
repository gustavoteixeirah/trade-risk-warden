package com.teixeirah.trw.application.ports.input;

import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.risk.DecisionType;
import com.teixeirah.trw.domain.user.ClientId;

import java.time.Instant;

public interface EnforceBreachInputPort {
    void handle(ClientId clientId, DecisionType type, Money loss, Money limit, Instant at);
}
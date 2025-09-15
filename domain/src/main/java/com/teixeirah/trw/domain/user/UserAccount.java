package com.teixeirah.trw.domain.user;

import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.risk.RiskLimits;

import java.time.ZoneId;

public record UserAccount(
        ClientId clientId,
        RiskLimits limits,
        InitialBalance initialBalance,
        ZoneId tz,
        String apiKey,
        String apiSecret) {

}



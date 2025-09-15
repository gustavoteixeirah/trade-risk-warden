package com.teixeirah.trw.application.dto;

import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.user.ClientId;
import java.time.ZoneId;
import java.util.Currency;

public record RegisterUserCommand(
    ClientId clientId,
    String apiKey,
    String apiSecret,
    RiskLimits limits,
    ZoneId tz,
    Currency currency
) {}



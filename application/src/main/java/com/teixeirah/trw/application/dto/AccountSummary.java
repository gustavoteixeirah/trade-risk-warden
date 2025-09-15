package com.teixeirah.trw.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountSummary(
        BigDecimal equity, String currency,
        BigDecimal realizedPnl, BigDecimal unrealizedPnl, Instant ts
) {
}

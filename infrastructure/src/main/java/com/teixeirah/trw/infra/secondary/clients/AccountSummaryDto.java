package com.teixeirah.trw.infra.secondary.clients;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountSummaryDto(
        String account,
        Map<String, BigDecimal> balances,

        @JsonProperty("cash_excess")
        BigDecimal cashExcess,

        BigDecimal equity,

        @JsonProperty("position_margin")
        BigDecimal positionMargin,

        // key = symbol (e.g., "ES 20250919 CME Future/USD")
        Map<String, List<PositionEntry>> positions,

        @JsonProperty("purchasing_power")
        BigDecimal purchasingPower,

        @JsonProperty("realized_pnl")
        BigDecimal realizedPnl,

        OffsetDateTime timestamp,

        @JsonProperty("total_margin")
        BigDecimal totalMargin,

        @JsonProperty("unrealized_pnl")
        BigDecimal unrealizedPnl,

        @JsonProperty("yesterday_equity")
        BigDecimal yesterdayEquity
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PositionEntry(
            @JsonProperty("break_even_price") BigDecimal breakEvenPrice,
            @JsonProperty("cost_basis") BigDecimal costBasis,
            @JsonProperty("liquidation_price") BigDecimal liquidationPrice,
            BigDecimal quantity,
            @JsonProperty("trade_time") OffsetDateTime tradeTime,
            @JsonProperty("unrealized_pnl") BigDecimal unrealizedPnl
    ) {
    }
}
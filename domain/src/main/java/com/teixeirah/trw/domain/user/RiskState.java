package com.teixeirah.trw.domain.user;


import java.time.Instant;

public record RiskState(
        boolean dailyBlocked,
        boolean permanentBlocked,
        Instant dailyBlockedAt,
        Instant permanentBlockedAt
) {
    public RiskState withDailyBlock(Instant at) {
        if (dailyBlocked) return this;
        return new RiskState(true, permanentBlocked, at, permanentBlockedAt);
    }

    public RiskState withPermanentBlock(Instant at) {
        if (permanentBlocked) return this;
        return new RiskState(dailyBlocked, true, dailyBlockedAt, at);
    }
}
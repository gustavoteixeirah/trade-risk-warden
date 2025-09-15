package com.teixeirah.trw.domain.user;


import com.teixeirah.trw.domain.money.Balance;
import com.teixeirah.trw.domain.money.Money;

import java.time.Instant;

public record PnlSnapshot(
        ClientId clientId,
        Instant ts,
        Balance currentBalance,
        Money realizedPnlToday,
        Money cumulativePnl) {
}
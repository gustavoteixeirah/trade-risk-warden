package com.teixeirah.trw.domain.trading;

import com.teixeirah.trw.domain.user.ClientId;

import java.math.BigDecimal;
import java.util.List;

public interface TradingPort {
    record OpenPosition(String symbol, BigDecimal qty) {}
    List<OpenPosition> listOpenPositions(ClientId id);
    void cancelOpenOrders(ClientId id);
    void closeAllAtMarket(ClientId id);
}


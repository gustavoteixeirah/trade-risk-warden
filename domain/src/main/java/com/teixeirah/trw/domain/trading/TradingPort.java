package com.teixeirah.trw.domain.trading;

import com.teixeirah.trw.domain.user.ClientId;

public interface TradingPort {

    void cancelOpenOrders(ClientId id);

    void closeAllAtMarket(ClientId id);

}


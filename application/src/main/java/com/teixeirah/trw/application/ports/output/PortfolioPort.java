package com.teixeirah.trw.application.ports.output;

import com.teixeirah.trw.application.dto.AccountSummary;

public interface PortfolioPort {
    AccountSummary fetch(String apiKey, String apiSecret);
}


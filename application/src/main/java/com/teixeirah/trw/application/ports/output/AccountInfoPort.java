package com.teixeirah.trw.application.ports.output;

import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import java.util.Currency;

public interface AccountInfoPort {
  Money getInitialBalanceForRegistration(String apiKey, String apiSecret, Currency preferredCurrency);
}



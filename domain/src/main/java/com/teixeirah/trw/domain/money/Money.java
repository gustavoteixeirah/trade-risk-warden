package com.teixeirah.trw.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) throw new IllegalArgumentException();
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}



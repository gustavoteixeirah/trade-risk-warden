package com.teixeirah.trw.domain.risk;

import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.UserAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RiskCalculatorTest {

    private static final Currency USD = Currency.getInstance("USD");

    private static UserAccount user(BigDecimal initial, BigDecimal daily, ThresholdType dailyType, BigDecimal max, ThresholdType maxType) {
        var limits = new RiskLimits(new RiskThreshold(dailyType, daily), new RiskThreshold(maxType, max));
        return new UserAccount(new ClientId("u1"), limits, new InitialBalance(new Money(initial, USD)), ZoneId.of("UTC"), "k", "s");
    }

    private static PnlSnapshot snapshot(BigDecimal equity, BigDecimal realized, BigDecimal cumulative) {
        return new PnlSnapshot(new ClientId("u1"), Instant.now(), new com.teixeirah.trw.domain.money.Balance(new Money(equity, USD)),
                new Money(realized, USD), new Money(cumulative, USD));
    }

    @Test
    void noChecks_returnsEmpty() {
        var calc = new RiskCalculator(List.of());
        var res = calc.evaluate(user(new BigDecimal("1000"), BigDecimal.TEN, ThresholdType.ABSOLUTE, BigDecimal.TEN, ThresholdType.ABSOLUTE),
                snapshot(new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO));
        assertTrue(res.isEmpty());
    }

    @Test
    void dailyCheck_triggers_whenDailyLossReachesLimit() {
        var calc = new RiskCalculator(List.of(new com.teixeirah.trw.domain.risk.checks.DailyRiskCheck()));

        var u = user(new BigDecimal("1000"), new BigDecimal("100"), ThresholdType.ABSOLUTE, new BigDecimal("9999"), ThresholdType.ABSOLUTE);
        var snap = snapshot(new BigDecimal("900"), new BigDecimal("-100"), BigDecimal.ZERO);

        var res = calc.evaluate(u, snap);
        assertTrue(res.isPresent());
        var d = res.get();
        assertEquals(DecisionType.DAILY_BREACH, d.type());
        assertEquals(0, d.loss().amount().compareTo(new BigDecimal("100")));
        assertEquals(USD, d.loss().currency());
        assertEquals(0, d.limit().amount().compareTo(new BigDecimal("100")));
        assertEquals(USD, d.limit().currency());
    }

    @Test
    void maxCheck_triggers_whenTotalLossReachesLimit() {
        var calc = new RiskCalculator(List.of(new com.teixeirah.trw.domain.risk.checks.MaxRiskCheck()));

        var u = user(new BigDecimal("1000"), new BigDecimal("9999"), ThresholdType.ABSOLUTE, new BigDecimal("100"), ThresholdType.ABSOLUTE);
        var snap = snapshot(new BigDecimal("900"), BigDecimal.ZERO, new BigDecimal("-100"));

        var res = calc.evaluate(u, snap);
        assertTrue(res.isPresent());
        var d = res.get();
        assertEquals(DecisionType.MAX_BREACH, d.type());
        assertEquals(0, d.loss().amount().compareTo(new BigDecimal("100")));
        assertEquals(USD, d.loss().currency());
        assertEquals(0, d.limit().amount().compareTo(new BigDecimal("100")));
        assertEquals(USD, d.limit().currency());
    }

    @Test
    void calculator_respects_priority_order() {
        // Max has higher priority (10) than Daily (20)
        var calc = new RiskCalculator(List.of(new com.teixeirah.trw.domain.risk.checks.DailyRiskCheck(), new com.teixeirah.trw.domain.risk.checks.MaxRiskCheck()));

        var u = user(new BigDecimal("1000"), new BigDecimal("50"), ThresholdType.ABSOLUTE, new BigDecimal("50"), ThresholdType.ABSOLUTE);
        var snap = snapshot(new BigDecimal("900"), new BigDecimal("-100"), new BigDecimal("-100"));

        var res = calc.evaluate(u, snap);
        assertTrue(res.isPresent());
        var d = res.get();
        assertEquals(DecisionType.MAX_BREACH, d.type());
        assertEquals(0, d.loss().amount().compareTo(new BigDecimal("100")));
        assertEquals(USD, d.loss().currency());
        assertEquals(0, d.limit().amount().compareTo(new BigDecimal("50")));
        assertEquals(USD, d.limit().currency());
    }
}



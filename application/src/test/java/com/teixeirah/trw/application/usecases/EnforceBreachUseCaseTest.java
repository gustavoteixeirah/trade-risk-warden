package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.DecisionType;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.trading.TradingPort;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.RiskState;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EnforceBreachUseCaseTest {

    private TradingPort trading;
    private UserAccountRepository users;
    private Notifier notifier;
    private EnforceBreachUseCase useCase;

    private final ClientId clientId = new ClientId("c-1");
    private final Currency USD = Currency.getInstance("USD");
    private final ZoneId TZ = ZoneId.of("America/Sao_Paulo");

    @BeforeEach
    void setUp() {
        trading = mock(TradingPort.class);
        users = mock(UserAccountRepository.class);
        notifier = mock(Notifier.class);
        useCase = new EnforceBreachUseCase(trading, users, notifier);
    }

    @Test
    void noneDecision_shortCircuits() {
        var user = user(false, false, null, null);
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.NONE, money("10"), money("20"), Instant.now());

        verifyNoInteractions(trading);
        verify(users, never()).save(any());
        verify(notifier, never()).publish(any());
    }

    @Test
    void maxBreach_alreadyPermanentBlocked_shortCircuits() {
        var user = user(false, true, null, Instant.now());
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.MAX_BREACH, money("10"), money("20"), Instant.now());

        verifyNoInteractions(trading);
        verify(users, never()).save(any());
        verify(notifier, never()).publish(any());
    }

    @Test
    void dailyBreach_sameDayAlreadyBlocked_shortCircuits() {
        Instant at = Instant.parse("2024-06-01T12:00:00Z");
        var user = user(true, false, at, null);
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.DAILY_BREACH, money("10"), money("20"), at);

        verifyNoInteractions(trading);
        verify(users, never()).save(any());
        verify(notifier, never()).publish(any());
    }

    @Test
    void dailyBreach_enforcesAndSavesAndNotifies() {
        Instant blockAt = Instant.parse("2024-06-01T12:00:00Z");
        var user = user(false, false, null, null);
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.DAILY_BREACH, money("15"), money("100"), blockAt);

        verify(trading).flattenAll(user.apiKey(), user.apiSecret());
        verify(users).save(argThat(updated -> updated.state().dailyBlocked() && blockAt.equals(updated.state().dailyBlockedAt())));

        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(event.capture());
        assertEquals(EventType.DAILY_RISK_BREACH_ACTION_APPLIED, event.getValue().type());
        assertEquals(clientId, event.getValue().clientId());
        Map<String, Object> details = event.getValue().details();
        assertEquals("flattenAll", details.get("action"));
        assertEquals("USD", details.get("currency"));
        assertEquals(new BigDecimal("15.00"), details.get("loss"));
        assertEquals(new BigDecimal("100.00"), details.get("limit"));
    }

    @Test
    void dailyBreach_newDay_enforces() {
        // daily blocked at 23:00Z (20:00 local), and at 05:00Z next day (02:00 local) -> different local day
        Instant yesterday = Instant.parse("2024-06-01T23:00:00Z");
        Instant today = Instant.parse("2024-06-02T05:00:00Z");
        var user = user(true, false, yesterday, null);
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.DAILY_BREACH, money("10"), money("50"), today);

        verify(trading).flattenAll(user.apiKey(), user.apiSecret());
        verify(users).save(any());
        verify(notifier).publish(any());
    }

    @Test
    void maxBreach_enforcesAndSavesAndNotifies() {
        Instant blockAt = Instant.parse("2024-06-01T12:00:00Z");
        var user = user(false, false, null, null);
        when(users.find(clientId)).thenReturn(Optional.of(user));

        useCase.handle(clientId, DecisionType.MAX_BREACH, money("20"), money("200"), blockAt);

        verify(trading).flattenAll(user.apiKey(), user.apiSecret());
        verify(users).save(argThat(updated -> updated.state().permanentBlocked() && blockAt.equals(updated.state().permanentBlockedAt())));

        ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(event.capture());
        assertEquals(EventType.MAX_RISK_BREACH_ACTION_APPLIED, event.getValue().type());
        assertEquals(clientId, event.getValue().clientId());
        Map<String, Object> details = event.getValue().details();
        assertEquals("flattenAll", details.get("action"));
        assertEquals("USD", details.get("currency"));
        assertEquals(new BigDecimal("20.00"), details.get("loss"));
        assertEquals(new BigDecimal("200.00"), details.get("limit"));
    }

    private Money money(String amt) {
        return new Money(new BigDecimal(amt), USD);
    }

    private UserAccount user(boolean daily, boolean permanent, Instant dailyAt, Instant permanentAt) {
        var limits = new RiskLimits(new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("100")),
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("1000")));
        var state = new RiskState(daily, permanent, dailyAt, permanentAt);
        return new UserAccount(clientId, limits, new com.teixeirah.trw.domain.money.InitialBalance(money("1000")),
                TZ, "k", "s", state);
    }
}



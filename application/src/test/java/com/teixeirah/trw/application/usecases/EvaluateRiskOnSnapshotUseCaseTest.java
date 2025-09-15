package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

import static com.teixeirah.trw.domain.notification.EventType.DAILY_RISK_TRIGGERED;
import static com.teixeirah.trw.domain.notification.EventType.MAX_RISK_TRIGGERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EvaluateRiskOnSnapshotUseCaseTest {

    private UserAccountRepository userRepo;
    private PnlSnapshotRepository snapshotRepo;
    private Notifier notifier;
    private EvaluateRiskOnSnapshotUseCase useCase;

    private final ClientId clientId = new ClientId("c-1");
    private final Currency USD = Currency.getInstance("USD");

    @BeforeEach
    void setUp() {
        userRepo = mock(UserAccountRepository.class);
        snapshotRepo = mock(PnlSnapshotRepository.class);
        notifier = mock(Notifier.class);
        useCase = new EvaluateRiskOnSnapshotUseCase(userRepo, snapshotRepo, notifier);
    }

    @Test
    void publishesMaxBreachWhenCumulativeLossExceedsMaxLimit() {
        var user = TestData.user(USD, 1000);
        var snap = TestData.snapshot(clientId, 1000, -1200, 0, USD, Instant.now());
        when(userRepo.find(clientId)).thenReturn(Optional.of(user));
        when(snapshotRepo.findLatest(clientId)).thenReturn(Optional.of(snap));

        useCase.handle(clientId);

        ArgumentCaptor<Event> cap = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(cap.capture());
        assertEquals(MAX_RISK_TRIGGERED, cap.getValue().type());
        assertEquals(clientId, cap.getValue().clientId());
    }

    @Test
    void publishesDailyBreachWhenRealizedLossExceedsDailyLimit() {
        var user = TestData.user(USD, 1000);
        var snap = TestData.snapshot(clientId, 1000, 0, -600, USD, Instant.now());
        when(userRepo.find(clientId)).thenReturn(Optional.of(user));
        when(snapshotRepo.findLatest(clientId)).thenReturn(Optional.of(snap));

        useCase.handle(clientId);

        ArgumentCaptor<Event> cap = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(cap.capture());
        assertEquals(DAILY_RISK_TRIGGERED, cap.getValue().type());
    }

    @Test
    void noEventWhenNoBreach() {
        var user = TestData.user(USD, 1000);
        var snap = TestData.snapshot(clientId, 1000, 100, 50, USD, Instant.now());
        when(userRepo.find(clientId)).thenReturn(Optional.of(user));
        when(snapshotRepo.findLatest(clientId)).thenReturn(Optional.of(snap));

        useCase.handle(clientId);

        verify(notifier, never()).publish(any());
    }

    private static class TestData {
        static UserAccount user(Currency cur, int initial) {
            var limits = new com.teixeirah.trw.domain.risk.RiskLimits(
                    new com.teixeirah.trw.domain.risk.RiskThreshold(com.teixeirah.trw.domain.risk.ThresholdType.ABSOLUTE, new BigDecimal("500")),
                    new com.teixeirah.trw.domain.risk.RiskThreshold(com.teixeirah.trw.domain.risk.ThresholdType.ABSOLUTE, new BigDecimal("1000"))
            );
            var ib = new com.teixeirah.trw.domain.money.InitialBalance(new com.teixeirah.trw.domain.money.Money(new BigDecimal(initial), cur));
            return new UserAccount(new ClientId("c-1"), limits, ib, java.time.ZoneId.of("America/Sao_Paulo"), "k", "s",
                    new RiskState(false, false, null, null));
        }

        static PnlSnapshot snapshot(ClientId id, int equity, int cumulative, int realizedToday, Currency cur, Instant ts) {
            var eq = new com.teixeirah.trw.domain.money.Money(new BigDecimal(equity), cur);
            var bal = new com.teixeirah.trw.domain.money.Balance(eq);
            var cum = new com.teixeirah.trw.domain.money.Money(new BigDecimal(cumulative), cur);
            var r = new com.teixeirah.trw.domain.money.Money(new BigDecimal(realizedToday), cur);
            return new PnlSnapshot(id, ts, bal, r, cum);
        }
    }
}



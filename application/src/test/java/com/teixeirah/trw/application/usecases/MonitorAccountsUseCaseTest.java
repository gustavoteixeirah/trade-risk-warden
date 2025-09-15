package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.dto.AccountInformationForMonitoring;
import com.teixeirah.trw.application.dto.AccountSummary;
import com.teixeirah.trw.application.ports.output.AccountInformationForMonitoringPort;
import com.teixeirah.trw.application.ports.output.PortfolioPort;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.PnlSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.teixeirah.trw.domain.notification.EventType.NEW_SNAPSHOT_GENERATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MonitorAccountsUseCaseTest {

    private AccountInformationForMonitoringPort infoPort;
    private PortfolioPort portfolio;
    private PnlSnapshotRepository repo;
    private Notifier notifier;
    private MonitorAccountsUseCase useCase;

    @BeforeEach
    void setUp() {
        infoPort = mock(AccountInformationForMonitoringPort.class);
        portfolio = mock(PortfolioPort.class);
        repo = mock(PnlSnapshotRepository.class);
        notifier = mock(Notifier.class);
        useCase = new MonitorAccountsUseCase(infoPort, portfolio, repo, notifier);
    }

    @Test
    void savesAndPublishesWhenSnapshotChanged() {
        var id = new ClientId("c-1");
        var creds = new AccountInformationForMonitoring(id, "k", "s", null);
        when(infoPort.fetchAccountInformationForMonitoring()).thenReturn(List.of(creds));
        when(portfolio.fetch("k", "s")).thenReturn(new AccountSummary(new BigDecimal("100"), "USD", new BigDecimal("10"), new BigDecimal("0"), Instant.now()));

        useCase.run();

        verify(repo).save(any(PnlSnapshot.class));
        ArgumentCaptor<Event> ev = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(ev.capture());
        assertEquals(NEW_SNAPSHOT_GENERATED, ev.getValue().type());
    }

    @Test
    void doesNothingWhenSnapshotEqual() {
        var id = new ClientId("c-1");
        var last = dummySnapshot(id);
        var creds = new AccountInformationForMonitoring(id, "k", "s", last);
        when(infoPort.fetchAccountInformationForMonitoring()).thenReturn(List.of(creds));
        when(portfolio.fetch("k", "s")).thenReturn(new AccountSummary(new BigDecimal("100.00"), "USD", new BigDecimal("10.00"), new BigDecimal("0.00"), last.ts()));

        useCase.run();

        verify(repo, never()).save(any());
        verify(notifier, never()).publish(any());
    }

    private static PnlSnapshot dummySnapshot(ClientId id) {
        var eq = new com.teixeirah.trw.domain.money.Money(new BigDecimal("100"), java.util.Currency.getInstance("USD"));
        var bal = new com.teixeirah.trw.domain.money.Balance(eq);
        var realized = new com.teixeirah.trw.domain.money.Money(new BigDecimal("10"), java.util.Currency.getInstance("USD"));
        var cum = new com.teixeirah.trw.domain.money.Money(new BigDecimal("10"), java.util.Currency.getInstance("USD"));
        return new PnlSnapshot(id, Instant.now(), bal, realized, cum);
    }
}



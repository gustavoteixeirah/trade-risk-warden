package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.dto.AccountSummary;
import com.teixeirah.trw.application.dto.AccountInformationForMonitoring;
import com.teixeirah.trw.application.ports.input.MonitorAccountsInputPort;
import com.teixeirah.trw.application.ports.output.PortfolioPort;
import com.teixeirah.trw.application.ports.output.AccountInformationForMonitoringPort;
import com.teixeirah.trw.domain.money.Balance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.PnlSnapshotRepository;

import java.math.BigDecimal;
import java.util.Currency;

public class MonitorAccountsUseCase implements MonitorAccountsInputPort {

    private final AccountInformationForMonitoringPort accountInformationForMonitoringPort;
    private final PortfolioPort portfolioPort;
    private final PnlSnapshotRepository pnlSnapshotRepository;

    public MonitorAccountsUseCase(AccountInformationForMonitoringPort accountInformationForMonitoringPort,
                                  PortfolioPort portfolioPort,
                                  PnlSnapshotRepository pnlSnapshotRepository) {
        this.accountInformationForMonitoringPort = accountInformationForMonitoringPort;
        this.portfolioPort = portfolioPort;
        this.pnlSnapshotRepository = pnlSnapshotRepository;
    }

    public void run() {
        for (final var accountInformation : accountInformationForMonitoringPort.fetchAccountInformationForMonitoring()) {
            runFor(accountInformation);
        }
    }

    public void runFor(AccountInformationForMonitoring credentials) {
        System.out.println("running for: "+ credentials.clientId());
        final var summary = portfolioPort.fetch(credentials.apiKey(), credentials.apiSecret());
        final var snapshot = map(credentials.clientId().value(), summary);
        final var last = credentials.lastSnapshot();
        if (!equalsIgnoringTs(snapshot, last)) {
            pnlSnapshotRepository.save(snapshot);
        }
    }

    private static PnlSnapshot map(String clientId, AccountSummary summary) {
        final var currency = Currency.getInstance(summary.currency());

        final var equity = money(summary.equity(), currency);
        final var realized = money(summary.realizedPnl(), currency);
        final var unrealized = money(summary.unrealizedPnl(), currency);

        final var unrealizedNonNegative = unrealized.amount().signum() < 0
                ? money(BigDecimal.ZERO, currency)
                : unrealized;

        final var cumulative = money(realized.amount().add(unrealizedNonNegative.amount()), currency);

        return new PnlSnapshot(
                new ClientId(clientId),
                summary.ts(),
                new Balance(equity),
                realized,
                cumulative
        );
    }

    private static Money money(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    private static boolean equalsIgnoringTs(PnlSnapshot a, PnlSnapshot b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!safeEquals(a.clientId(), b.clientId())) return false;
        if (!equalsBalance(a.currentBalance(), b.currentBalance())) return false;
        if (!equalsMoney(a.realizedPnlToday(), b.realizedPnlToday())) return false;
        if (!equalsMoney(a.cumulativePnl(), b.cumulativePnl())) return false;
        return true;
    }

    private static boolean equalsBalance(Balance a, Balance b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return equalsMoney(a.value(), b.value());
    }

    private static boolean equalsMoney(Money a, Money b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.currency() == null ? b.currency() != null : !a.currency().equals(b.currency())) return false;
        // Use compareTo to ignore scale differences
        return a.amount().compareTo(b.amount()) == 0;
    }

    private static boolean safeEquals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}

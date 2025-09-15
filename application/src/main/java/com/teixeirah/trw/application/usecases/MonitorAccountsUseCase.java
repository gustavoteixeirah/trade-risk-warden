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
        if (!snapshot.equals(last)) {
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
}

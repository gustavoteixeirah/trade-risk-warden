package com.teixeirah.trw.infra.bootstrap;

import com.teixeirah.trw.application.ports.input.MonitorAccountsInputPort;
import com.teixeirah.trw.application.ports.input.RegisterUserInputPort;
import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.application.ports.output.AccountInformationForMonitoringPort;
import com.teixeirah.trw.application.ports.output.PortfolioPort;
import com.teixeirah.trw.application.usecases.MonitorAccountsUseCase;
import com.teixeirah.trw.application.usecases.RegisterUserUseCase;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.user.PnlSnapshotRepository;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling
public class UseCaseConfig {

    @Bean
    RegisterUserInputPort registerUserInputPort(UserAccountRepository userRepo, Notifier notifier, AccountInfoPort accountInfo) {
        return new RegisterUserUseCase(userRepo, notifier, accountInfo);
    }

    @Bean
    MonitorAccountsInputPort monitorAccountsInputPort(AccountInformationForMonitoringPort registeredUsers,
                                                      PortfolioPort portfolio,
                                                      PnlSnapshotRepository pnlSnapshots) {
        return new MonitorAccountsUseCase(registeredUsers, portfolio, pnlSnapshots);
    }

}



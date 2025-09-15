package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.dto.RegisterUserCommand;
import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Optional;

import static com.teixeirah.trw.domain.notification.EventType.USER_REGISTERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserAccountRepository userRepo;
    private Notifier notifier;
    private AccountInfoPort accountInfo;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserAccountRepository.class);
        notifier = mock(Notifier.class);
        accountInfo = mock(AccountInfoPort.class);
        useCase = new RegisterUserUseCase(userRepo, notifier, accountInfo);
    }

    @Test
    void registersUserAndPublishesEvent() {
        var cmd = command("c-1");
        when(userRepo.find(cmd.clientId())).thenReturn(Optional.empty());
        when(accountInfo.getInitialBalanceForRegistration("k","s", cmd.currency()))
                .thenReturn(new Money(new BigDecimal("1000"), cmd.currency()));

        useCase.handle(cmd);

        verify(userRepo).save(any());
        ArgumentCaptor<Event> ev = ArgumentCaptor.forClass(Event.class);
        verify(notifier).publish(ev.capture());
        assertEquals(USER_REGISTERED, ev.getValue().type());
        assertEquals(cmd.clientId(), ev.getValue().clientId());
    }

    @Test
    void failsWhenClientAlreadyExists() {
        var cmd = command("c-1");
        when(userRepo.find(cmd.clientId())).thenReturn(Optional.of(mock(com.teixeirah.trw.domain.user.UserAccount.class)));

        assertThrows(IllegalArgumentException.class, () -> useCase.handle(cmd));
        verifyNoInteractions(accountInfo);
    }

    @Test
    void failsWhenInitialBalanceMissing() {
        var cmd = command("c-1");
        when(userRepo.find(cmd.clientId())).thenReturn(Optional.empty());
        when(accountInfo.getInitialBalanceForRegistration(any(), any(), any())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> useCase.handle(cmd));
        verify(userRepo, never()).save(any());
    }

    private static RegisterUserCommand command(String id) {
        var limits = new RiskLimits(
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("500")),
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("1000"))
        );
        return new RegisterUserCommand(new ClientId(id), "k", "s", limits, ZoneId.of("America/Sao_Paulo"), Currency.getInstance("USD"));
    }
}



package com.teixeirah.trw.application.ports.input;

import com.teixeirah.trw.application.dto.RegisterUserCommand;
import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.application.usecases.RegisterUserUseCase;
import com.teixeirah.trw.domain.audit.AuditEvent;
import com.teixeirah.trw.domain.audit.AuditRepository;
import com.teixeirah.trw.domain.audit.AuditType;
import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public final class RegisterUserInputPort implements RegisterUserUseCase {

    private final UserAccountRepository userRepo;
  private final AuditRepository auditRepo;
  private final Notifier notifier;
  private final AccountInfoPort accountInfo;

  public RegisterUserInputPort(UserAccountRepository userRepo, AuditRepository auditRepo, Notifier notifier, AccountInfoPort accountInfo) {
    this.userRepo = userRepo;
    this.auditRepo = auditRepo;
    this.notifier = notifier;
    this.accountInfo = accountInfo;
  }

  @Override
  public void handle(final RegisterUserCommand cmd) {
    validate(cmd.clientId(), cmd.limits());

    userRepo.find(cmd.clientId()).ifPresent(u -> { throw new IllegalArgumentException("clientId already exists"); });

    Money initial = Optional.ofNullable(accountInfo.getInitialBalanceForRegistration(cmd.apiKey(), cmd.apiSecret(), cmd.currency()))
        .orElseThrow(() -> new IllegalStateException("cannot fetch account balance"));

    var ua = UserAccount.register(cmd.clientId(), cmd.limits(), new InitialBalance(initial), cmd.tz(), "");
    userRepo.save(ua);

    var event = new AuditEvent(cmd.clientId(), Instant.now(), AuditType.USER_REGISTERED, null, null,
        "User registered", null, Map.of("tz", cmd.tz().toString()));
    auditRepo.save(event);
    notifier.publish(event);
  }

  private void validate(ClientId id, RiskLimits limits) {
    if (id == null) throw new IllegalArgumentException("clientId");

    if (limits == null) throw new IllegalArgumentException("limits");

    if (limits.daily() == null || limits.max() == null) throw new IllegalArgumentException("limits fields");

    var d = limits.daily().value();

    var m = limits.max().value();

    if (d.signum() < 0 || m.signum() < 0) throw new IllegalArgumentException("limit < 0");

    if (limits.daily().type() == ThresholdType.PERCENTAGE && d.compareTo(BigDecimal.valueOf(100)) > 0)
      throw new IllegalArgumentException("daily % > 100");

    if (limits.max().type() == ThresholdType.PERCENTAGE && m.compareTo(BigDecimal.valueOf(100)) > 0)
      throw new IllegalArgumentException("max % > 100");
  }
}


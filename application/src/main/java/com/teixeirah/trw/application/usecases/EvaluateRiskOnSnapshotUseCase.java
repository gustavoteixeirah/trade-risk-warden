package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.ports.input.EvaluateRiskOnSnapshotInputPort;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.notification.Notifier;
import com.teixeirah.trw.domain.risk.DecisionType;
import com.teixeirah.trw.domain.risk.RiskCalculator;
import com.teixeirah.trw.domain.risk.checks.DailyRiskCheck;
import com.teixeirah.trw.domain.risk.checks.MaxRiskCheck;
import com.teixeirah.trw.domain.user.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EvaluateRiskOnSnapshotUseCase implements EvaluateRiskOnSnapshotInputPort {

    private final UserAccountRepository userRepo;
    private final PnlSnapshotRepository snapshotRepo;
    private final Notifier notifier;

    public EvaluateRiskOnSnapshotUseCase(UserAccountRepository userRepo, PnlSnapshotRepository snapshotRepo, Notifier notifier) {
        this.userRepo = userRepo;
        this.snapshotRepo = snapshotRepo;
        this.notifier = notifier;
    }

    private static final RiskCalculator calculator =
            new RiskCalculator(List.of(new MaxRiskCheck(), new DailyRiskCheck()));

    @Override
    public void handle(final ClientId clientId) {
        final var user = userRepo.find(clientId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + clientId.value()));
        final var snap = snapshotRepo.findLatest(clientId)
                .orElseThrow(() -> new IllegalStateException("No snapshot for: " + clientId.value()));

        calculator.evaluate(user, snap).ifPresent(decision -> {
            notifier.publish(new Event(
                    clientId,
                    Instant.now(),
                    toEventType(decision.type()),
                    Map.of(
                            "loss", decision.loss().amount(),
                            "limit", decision.limit().amount(),
                            "currency", decision.limit().currency().getCurrencyCode(),
                            "decision", decision.type().name(),
                            "snapshotTs", snap.ts().toString()
                    )
            ));
        });
    }

    private static EventType toEventType(DecisionType t) {
        return switch (t) {
            case MAX_BREACH -> EventType.MAX_RISK_TRIGGERED;
            case DAILY_BREACH -> EventType.DAILY_RISK_TRIGGERED;
            default -> null;
        };
    }
}

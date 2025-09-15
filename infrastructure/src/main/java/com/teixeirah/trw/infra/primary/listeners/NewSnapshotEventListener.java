package com.teixeirah.trw.infra.primary.listeners;

import com.teixeirah.trw.application.ports.input.EvaluateRiskOnSnapshotInputPort;
import com.teixeirah.trw.domain.notification.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewSnapshotEventListener {

    private final EvaluateRiskOnSnapshotInputPort evaluateRiskOnSnapshot;

    @EventListener(condition = "#e.type == T(com.teixeirah.trw.domain.notification.EventType).NEW_SNAPSHOT_GENERATED")
    public void on(Event e) {
        log.debug("Handling NEW_SNAPSHOT_GENERATED event for clientId={}", e.clientId().value());
        evaluateRiskOnSnapshot.handle(e.clientId());
    }
}
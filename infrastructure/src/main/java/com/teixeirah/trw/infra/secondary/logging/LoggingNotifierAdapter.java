package com.teixeirah.trw.infra.secondary.logging;

import com.teixeirah.trw.domain.audit.AuditEvent;
import com.teixeirah.trw.domain.notification.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class LoggingNotifierAdapter implements Notifier {

    @Override
    public void publish(AuditEvent e) {
        log.info("NOTIFY: type={}, client={}, msg={}", e.type(), e.clientId().value(), e.actionSummary());
    }

}


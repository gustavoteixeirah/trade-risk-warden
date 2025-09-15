package com.teixeirah.trw.infra.secondary.logging;

import com.teixeirah.trw.domain.audit.AuditEvent;
import com.teixeirah.trw.domain.audit.AuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class LoggingAuditRepositoryAdapter implements AuditRepository {

    @Override
    public void save(AuditEvent e) {
        log.info("AUDIT: type={}, client={}, msg={}", e.type(), e.clientId().value(), e.actionSummary());
    }

}


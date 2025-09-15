package com.teixeirah.trw.domain.audit;

import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import java.time.Instant;
import java.util.Map;

public record AuditEvent(
  ClientId clientId,
  Instant ts,
  AuditType type,
  Money loss,
  Money limit,
  String actionSummary,
  String correlationId,
  Map<String,Object> details
) {}



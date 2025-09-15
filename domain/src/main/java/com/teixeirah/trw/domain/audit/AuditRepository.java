package com.teixeirah.trw.domain.audit;

public interface AuditRepository {
  void save(AuditEvent e);
}



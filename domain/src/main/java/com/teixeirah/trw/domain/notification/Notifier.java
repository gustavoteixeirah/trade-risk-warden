package com.teixeirah.trw.domain.notification;

import com.teixeirah.trw.domain.audit.AuditEvent;

public interface Notifier {
  void publish(AuditEvent e);
}



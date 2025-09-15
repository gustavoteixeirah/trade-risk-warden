package com.teixeirah.trw.infra.secondary.notifications;

import com.teixeirah.trw.domain.notification.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingEventListener {

    @EventListener
    public void on(Event e) {
        log.info("NOTIFY: type={}, client={}, details={}",
                e.type(), e.clientId().value(), e.details());
    }

}

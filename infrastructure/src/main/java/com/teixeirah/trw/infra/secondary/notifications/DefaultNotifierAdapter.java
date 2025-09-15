package com.teixeirah.trw.infra.secondary.notifications;

import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.Notifier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DefaultNotifierAdapter implements Notifier {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Event e) {
        publisher.publishEvent(e);
    }
}

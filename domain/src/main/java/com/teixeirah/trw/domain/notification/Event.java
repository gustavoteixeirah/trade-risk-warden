package com.teixeirah.trw.domain.notification;

import com.teixeirah.trw.domain.user.ClientId;

import java.time.Instant;
import java.util.Map;

public record Event(
        ClientId clientId,
        Instant ts,
        EventType type,
        Map<String, Object> details
) {
}



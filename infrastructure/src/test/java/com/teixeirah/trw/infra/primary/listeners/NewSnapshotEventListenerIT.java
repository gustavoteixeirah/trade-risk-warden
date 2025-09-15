package com.teixeirah.trw.infra.primary.listeners;

import com.teixeirah.trw.domain.money.Balance;
import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.user.*;
import com.teixeirah.trw.infra.bootstrap.AppRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static java.time.Instant.now;
import static java.time.ZoneId.of;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(classes = AppRunner.class)
class NewSnapshotEventListenerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PnlSnapshotRepository snapshots;

    @Autowired
    private UserAccountRepository users;

    @Test
    void forwardsEventToUseCase() {
        var clientId = new ClientId("c-3");
        var usd = java.util.Currency.getInstance("USD");
        var limits = new RiskLimits(
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("500")),
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("1000"))
        );
        var ib = new InitialBalance(new Money(new BigDecimal("1000"), usd));
        var ua = new UserAccount(clientId, limits, ib, of("America/Sao_Paulo"), "k", "s", new RiskState(false, false, null, null));
        users.save(ua);
        var snap = new PnlSnapshot(clientId, now(), new Balance(new Money(new BigDecimal("100"), usd)), new Money(new BigDecimal("0"), usd), new Money(new BigDecimal("0"), usd));
        snapshots.save(snap);

        publisher.publishEvent(new Event(clientId, now(), EventType.NEW_SNAPSHOT_GENERATED, java.util.Map.of()));

        var latest = snapshots.findLatest(clientId);
        assertThat(latest).isPresent();
    }
}



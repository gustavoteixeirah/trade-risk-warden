package com.teixeirah.trw.infra.primary.listeners;

import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.notification.Event;
import com.teixeirah.trw.domain.notification.EventType;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.trading.TradingPort;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.application.ports.output.PortfolioPort;
import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.application.ports.output.AccountInformationForMonitoringPort;
import com.teixeirah.trw.domain.user.RiskState;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import com.teixeirah.trw.infra.bootstrap.AppRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(classes = AppRunner.class)
class BreachEnforcementListenerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private UserAccountRepository users;

    @MockBean
    private TradingPort tradingPort;

    @MockBean
    private PortfolioPort portfolioPort;

    @MockBean
    private AccountInfoPort accountInfoPort;

    @MockBean
    private AccountInformationForMonitoringPort accountInformationForMonitoringPort;


    @Test
    void dailyRiskEvent_invokesEnforceWithDailyBreach() {
        var clientId = new ClientId("c-1");
        seedUser(clientId);
        var at = Instant.parse("2024-06-01T12:00:00Z");
        Map<String, Object> details = new HashMap<>();
        details.put("loss", new BigDecimal("15.00"));
        details.put("limit", new BigDecimal("100.00"));
        details.put("currency", "USD");

        publisher.publishEvent(new Event(clientId, at, EventType.DAILY_RISK_TRIGGERED, details));

        var updated = users.find(clientId).orElseThrow();
        assertTrue(updated.state().dailyBlocked());
        assertEquals(at, updated.state().dailyBlockedAt());
    }

    @Test
    void maxRiskEvent_invokesEnforceWithMaxBreach() {
        var clientId = new ClientId("c-2");
        seedUser(clientId);
        var at = Instant.parse("2024-06-02T12:00:00Z");
        Map<String, Object> details = new HashMap<>();
        details.put("loss", new BigDecimal("250.00"));
        details.put("limit", new BigDecimal("200.00"));
        details.put("currency", "USD");

        publisher.publishEvent(new Event(clientId, at, EventType.MAX_RISK_TRIGGERED, details));

        var updated = users.find(clientId).orElseThrow();
        assertTrue(updated.state().permanentBlocked());
        assertEquals(at, updated.state().permanentBlockedAt());

    }

    private void seedUser(ClientId clientId) {
        var limits = new RiskLimits(
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("100")),
                new RiskThreshold(ThresholdType.ABSOLUTE, new BigDecimal("200"))
        );
        var ib = new InitialBalance(new Money(new BigDecimal("1000"), Currency.getInstance("USD")));
        var ua = new UserAccount(clientId, limits, ib, ZoneId.of("America/Sao_Paulo"), "k", "s", new RiskState(false, false, null, null));
        users.save(ua);
    }
}



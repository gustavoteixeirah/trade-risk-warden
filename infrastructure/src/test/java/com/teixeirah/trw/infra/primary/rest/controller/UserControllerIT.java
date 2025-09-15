package com.teixeirah.trw.infra.primary.rest.controller;

import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Currency;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Testcontainers
@ActiveProfiles("test")
@Import(UserControllerIT.TestStubs.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.teixeirah.trw.infra.bootstrap.AppRunner.class)
class UserControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @LocalServerPort
    int port;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @BeforeEach
    void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void registerUser_persistsDocument_andReturns201() {
        String clientId = "user123";

        final String payload = """
                {
                  "clientId": "%s",
                  "apiKey": "xxxxx",
                  "apiSecret": "yyyyy",
                  "maxRisk": { "type": "PERCENTAGE", "value": 30 },
                  "dailyRisk": { "type": "ABSOLUTE", "value": 5000 },
                  "currency":"USD"
                }
                """.formatted(clientId);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/users")
                .then()
                .statusCode(equalTo(201));
        UserAccount ua = userAccountRepository.find(new ClientId(clientId))
                .orElseThrow(() -> new AssertionError("UserAccount not found in Mongo for clientId=" + clientId));

        assertThat(ua.initialBalance().value().amount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(ua.initialBalance().value().currency().getCurrencyCode()).isEqualTo("USD");

        assertThat(ua.limits()).isNotNull();
        assertThat(ua.limits().max().type()).isEqualTo(ThresholdType.PERCENTAGE);
        assertThat(ua.limits().max().value()).isEqualByComparingTo("30");
        assertThat(ua.limits().daily().type()).isEqualTo(ThresholdType.ABSOLUTE);
        assertThat(ua.limits().daily().value()).isEqualByComparingTo("5000");
    }

    @TestConfiguration
    static class TestStubs {

        @Bean
        @Primary
        AccountInfoPort accountInfoPort() {
            return (apiKey, apiSecret, preferredCurrency) -> new Money(new BigDecimal("50000.00"), Currency.getInstance("USD"));
        }
    }
}



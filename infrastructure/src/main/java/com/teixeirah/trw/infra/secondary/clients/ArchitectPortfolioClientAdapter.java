package com.teixeirah.trw.infra.secondary.clients;

import com.teixeirah.trw.application.dto.AccountSummary;
import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.application.ports.output.PortfolioPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.trading.TradingPort;
import com.teixeirah.trw.domain.user.ClientId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
class ArchitectPortfolioClientAdapter implements AccountInfoPort, PortfolioPort, TradingPort {

    private final WebClient architectWebClient;

    @Override
    public Money getInitialBalanceForRegistration(String apiKey, String apiSecret, @Nullable Currency preferredCurrency) {
        try {
            AccountSummaryDto dto = architectWebClient.get()
                    .uri("/account-summary")
                    .header("api_key", apiKey)
                    .header("api_secret", apiSecret)
                    .retrieve()
                    .bodyToMono(AccountSummaryDto.class)
                    .block();

            if (dto == null) throw new ArchitectClientException(500, "empty response");

            String currencyCode = resolveCurrency(dto, preferredCurrency);
            BigDecimal equity = nonNull(dto.equity(), "equity");
            return new Money(equity, Currency.getInstance(currencyCode));

        } catch (WebClientResponseException e) {
            throw new ArchitectClientException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ArchitectClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchitectClientException(500, e.getMessage());
        }
    }

    @Override
    public AccountSummary fetch(String apiKey, String apiSecret) {
        try {
            final var dto = architectWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/account-summary")
                            .build())
                    .header("api_key", apiKey)
                    .header("api_secret", apiSecret)
                    .retrieve()
                    .bodyToMono(AccountSummaryDto.class)
                    .block();

            if (dto == null) throw new ArchitectClientException(500, "empty response");

            final var currency = resolveCurrency(dto, null);
            final var realized = defaultIfNull(dto.realizedPnl());
            final var unrealized = defaultIfNull(dto.unrealizedPnl());
            final var equity = nonNull(dto.equity(), "equity");
            final var ts = nonNull(dto.timestamp(), "timestamp").toInstant();

            return new AccountSummary(equity, currency, realized, unrealized, ts);

        } catch (WebClientResponseException e) {
            throw new ArchitectClientException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ArchitectClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchitectClientException(500, e.getMessage());
        }
    }

    private static String resolveCurrency(AccountSummaryDto dto, @Nullable Currency preferred) {
        if (preferred != null) return preferred.getCurrencyCode();

        Map<String, BigDecimal> balances = dto.balances();
        if (balances != null && !balances.isEmpty()) {
            return balances.keySet().iterator().next();
        }
        return "USD";
    }

    private static <T> T nonNull(T value, String fieldName) {
        if (value == null) throw new ArchitectClientException(500, "missing field: " + fieldName);
        return value;
    }

    private static BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @Override
    public void cancelOpenOrders(ClientId id) {
        log.info("No-op cancel open orders...Implement me :)");
    }

    @Override
    public void closeAllAtMarket(ClientId id) {
        log.info("No-op close all at market...Implement me :)");
    }
}

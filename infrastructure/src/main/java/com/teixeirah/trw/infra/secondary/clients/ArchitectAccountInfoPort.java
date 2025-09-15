package com.teixeirah.trw.infra.secondary.clients;

import com.teixeirah.trw.application.ports.output.AccountInfoPort;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
class ArchitectAccountInfoPort implements AccountInfoPort {

    private final WebClient architectWebClient;

    @Override
    public Money getInitialBalanceForRegistration(String apiKey, String apiSecret, java.util.Currency preferredCurrency) {
        try {
            Map<?, ?> resp = architectWebClient.get()
                    .uri("/initial-balance")
                    .header("api_key", apiKey)
                    .header("api_secret", apiSecret)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null) throw new ArchitectClientException(500, "empty response");

            Object balancesObj = resp.get("balances");
            if (!(balancesObj instanceof Map<?, ?> balancesMap)) {
                throw new ArchitectClientException(500, "invalid balances format");
            }

            String currencyCode = preferredCurrency != null ? preferredCurrency.getCurrencyCode() : "USD";
            Object amountObj = balancesMap.get(currencyCode);
            if (amountObj == null) {
                if (!balancesMap.isEmpty()) {
                    Map.Entry<?, ?> first = balancesMap.entrySet().iterator().next();
                    currencyCode = String.valueOf(first.getKey());
                    amountObj = first.getValue();
                } else {
                    throw new ArchitectClientException(500, "no balances returned");
                }
            }

            BigDecimal amount = new BigDecimal(String.valueOf(amountObj));
            java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
            return new Money(amount, currency);
        } catch (WebClientResponseException e) {
            throw new ArchitectClientException(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ArchitectClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchitectClientException(500, e.getMessage());
        }
    }
}



package com.teixeirah.trw.infra.secondary.persistence.document;

import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.risk.ThresholdType;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccount;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Currency;

@Document(collection = "user_accounts")
public class UserAccountDocument {

    @Id
    private String id; // clientId

    private ThresholdDoc dailyRisk;
    private ThresholdDoc maxRisk;
    private MoneyDoc initialBalance;
    private String tz;
    private String architectAccountId;
    private String apiKey;
    private String apiSecret;

    public static UserAccountDocument fromDomain(UserAccount u) {
        var d = new UserAccountDocument();
        d.id = u.clientId().value();
        d.dailyRisk = ThresholdDoc.fromDomain(u.limits().daily());
        d.maxRisk = ThresholdDoc.fromDomain(u.limits().max());
        d.initialBalance = MoneyDoc.fromDomain(u.initialBalance().value());
        d.tz = u.tz().getId();
        d.apiKey = u.apiKey();
        d.apiSecret = u.apiSecret();
        return d;
    }

    public UserAccount toDomain() {
        var limits = new RiskLimits(
                dailyRisk.toDomain(),
                maxRisk.toDomain()
        );
        return new UserAccount(new ClientId(id), limits, new InitialBalance(initialBalance.toDomain()), ZoneId.of(tz), apiKey, apiSecret);
    }

    public record ThresholdDoc(String type, BigDecimal value) {
        public static ThresholdDoc fromDomain(RiskThreshold t) {
            return new ThresholdDoc(t.type().name(), t.value());
        }

        public RiskThreshold toDomain() {
            return new RiskThreshold(ThresholdType.valueOf(type), value);
        }
    }

    public record MoneyDoc(BigDecimal amount, String currency) {

        public static MoneyDoc fromDomain(Money m) {
            return new MoneyDoc(m.amount(), m.currency().getCurrencyCode());
        }

        public Money toDomain() {
            return new Money(amount, Currency.getInstance(currency));
        }

    }
}



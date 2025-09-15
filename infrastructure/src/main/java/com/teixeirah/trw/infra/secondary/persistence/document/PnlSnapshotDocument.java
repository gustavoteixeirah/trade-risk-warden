package com.teixeirah.trw.infra.secondary.persistence.document;

import com.teixeirah.trw.domain.money.Balance;
import com.teixeirah.trw.domain.money.Money;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.PnlSnapshot;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

@Document(collection = "pnl_snapshots")
public class PnlSnapshotDocument {
    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private Instant ts;

    private MoneyDoc currentBalance;
    private MoneyDoc realizedPnlToday;
    private MoneyDoc cumulativePnl;

    public PnlSnapshotDocument() {
    }

    public PnlSnapshotDocument(String id, String clientId, Instant ts, MoneyDoc currentBalance, MoneyDoc realizedPnlToday, MoneyDoc cumulativePnl) {
        this.id = id;
        this.clientId = clientId;
        this.ts = ts;
        this.currentBalance = currentBalance;
        this.realizedPnlToday = realizedPnlToday;
        this.cumulativePnl = cumulativePnl;
    }

    public static PnlSnapshotDocument fromDomain(PnlSnapshot s) {
        return new PnlSnapshotDocument(
                null,
                s.clientId().value(),
                s.ts(),
                MoneyDoc.fromDomain(s.currentBalance().value()),
                s.realizedPnlToday() != null ? MoneyDoc.fromDomain(s.realizedPnlToday()) : null,
                s.cumulativePnl() != null ? MoneyDoc.fromDomain(s.cumulativePnl()) : null
        );
    }

    public PnlSnapshot toDomain() {
        Money bal = currentBalance.toDomain();
        Money rToday = realizedPnlToday != null ? realizedPnlToday.toDomain() : null;
        Money cum = cumulativePnl != null ? cumulativePnl.toDomain() : null;
        return new PnlSnapshot(new ClientId(clientId), ts, new Balance(bal), rToday, cum);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Instant getTs() {
        return ts;
    }

    public void setTs(Instant ts) {
        this.ts = ts;
    }

    public MoneyDoc getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(MoneyDoc currentBalance) {
        this.currentBalance = currentBalance;
    }

    public MoneyDoc getRealizedPnlToday() {
        return realizedPnlToday;
    }

    public void setRealizedPnlToday(MoneyDoc realizedPnlToday) {
        this.realizedPnlToday = realizedPnlToday;
    }

    public MoneyDoc getCumulativePnl() {
        return cumulativePnl;
    }

    public void setCumulativePnl(MoneyDoc cumulativePnl) {
        this.cumulativePnl = cumulativePnl;
    }

    @Setter
    @Getter
    public static class MoneyDoc {
        private BigDecimal amount;
        private String currency;

        public MoneyDoc() {
        }

        public MoneyDoc(BigDecimal amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        public static MoneyDoc fromDomain(Money m) {
            return new MoneyDoc(m.amount(), m.currency().getCurrencyCode());
        }

        public Money toDomain() {
            return new Money(amount, Currency.getInstance(currency));
        }

    }
}



package com.teixeirah.trw.domain.risk;

import com.teixeirah.trw.domain.money.Balance;
import java.time.Instant;

public final class RiskState {
  private boolean dailyBlocked;
  private boolean permanentBlocked;
  private Balance dayStartBalance;
  private Instant lastDailyReset;

  public void startNewDay(Balance start, Instant now) {
    this.dailyBlocked = false;
    this.dayStartBalance = start;
    this.lastDailyReset = now;
  }
  public void blockDaily(){ this.dailyBlocked = true; }
  public void blockPermanent(){ this.permanentBlocked = true; }
  public void unblockDaily(){ this.dailyBlocked = false; }
  public void unblockPermanent(){ this.permanentBlocked = false; }
  public boolean isDailyBlocked(){ return dailyBlocked; }
  public boolean isPermanentBlocked(){ return permanentBlocked; }
  public Balance dayStartBalance(){ return dayStartBalance; }
  public Instant lastDailyReset(){ return lastDailyReset; }
}



package com.teixeirah.trw.domain.user;

import com.teixeirah.trw.domain.money.Balance;
import com.teixeirah.trw.domain.money.InitialBalance;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskState;
import java.time.Instant;
import java.time.ZoneId;

public final class UserAccount {
  private final ClientId clientId;
  private final RiskLimits limits;
  private final InitialBalance initialBalance;
  private final ZoneId tz;
  private final String architectAccountId;
  private final RiskState state = new RiskState();

  private UserAccount(ClientId id, RiskLimits limits, InitialBalance initial, ZoneId tz, String archId){
    this.clientId=id; this.limits=limits; this.initialBalance=initial; this.tz=tz; this.architectAccountId=archId;
  }
  public static UserAccount register(ClientId id, RiskLimits limits, InitialBalance initial, ZoneId tz, String archId){
    var ua = new UserAccount(id, limits, initial, tz, archId);
    ua.state.startNewDay(new Balance(initial.value()), Instant.now());
    return ua;
  }

  public ClientId clientId(){ return clientId; }
  public RiskLimits limits(){ return limits; }
  public InitialBalance initialBalance(){ return initialBalance; }
  public RiskState state(){ return state; }
  public ZoneId tz(){ return tz; }
  public String architectAccountId(){ return architectAccountId; }
}



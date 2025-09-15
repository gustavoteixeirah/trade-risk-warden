package com.teixeirah.trw.domain.risk;

import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.UserAccount;

import java.util.Optional;

public interface RiskCheck {
    int priority();

    Optional<RiskDecision> evaluate(UserAccount user, PnlSnapshot snapshot);
}

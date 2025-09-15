package com.teixeirah.trw.domain.risk;

import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.UserAccount;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RiskCalculator {

    private final List<RiskCheck> checks;

    public RiskCalculator(List<RiskCheck> checks) {
        this.checks = checks.stream()
                .sorted(Comparator.comparingInt(RiskCheck::priority))
                .toList();
    }

    public Optional<RiskDecision> evaluate(UserAccount user, PnlSnapshot snapshot) {
        for (var c : checks) {
            var hit = c.evaluate(user, snapshot);
            if (hit.isPresent()) return hit;
        }
        return Optional.empty();
    }
}

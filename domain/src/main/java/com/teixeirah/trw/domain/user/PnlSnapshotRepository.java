package com.teixeirah.trw.domain.user;

import java.util.Optional;

public interface PnlSnapshotRepository {
    void save(PnlSnapshot s);

    Optional<PnlSnapshot> findLatest(ClientId clientId);
}


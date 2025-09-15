package com.teixeirah.trw.infra.secondary.persistence.adapter;

import com.teixeirah.trw.domain.user.PnlSnapshot;
import com.teixeirah.trw.domain.user.PnlSnapshotRepository;
import com.teixeirah.trw.infra.secondary.persistence.document.PnlSnapshotDocument;
import com.teixeirah.trw.infra.secondary.persistence.repo.PnlSnapshotMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MongoPnlSnapshotRepository implements PnlSnapshotRepository {

    private final PnlSnapshotMongoRepository repo;

    @Override
    public void save(PnlSnapshot s) {
        repo.save(PnlSnapshotDocument.fromDomain(s));
    }

}



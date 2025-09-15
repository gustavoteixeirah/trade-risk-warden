package com.teixeirah.trw.infra.secondary.persistence.repo;

import com.teixeirah.trw.infra.secondary.persistence.document.PnlSnapshotDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PnlSnapshotMongoRepository extends MongoRepository<PnlSnapshotDocument, String> {
    Optional<PnlSnapshotDocument> findFirstByClientIdOrderByTsDesc(String clientId);
}



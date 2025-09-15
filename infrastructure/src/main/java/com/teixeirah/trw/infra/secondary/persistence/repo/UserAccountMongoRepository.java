package com.teixeirah.trw.infra.secondary.persistence.repo;

import com.teixeirah.trw.infra.secondary.persistence.document.UserAccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountMongoRepository extends MongoRepository<UserAccountDocument, String> {
}



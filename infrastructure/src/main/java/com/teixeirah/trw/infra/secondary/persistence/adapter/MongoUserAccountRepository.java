package com.teixeirah.trw.infra.secondary.persistence.adapter;

import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.UserAccount;
import com.teixeirah.trw.domain.user.UserAccountRepository;
import com.teixeirah.trw.infra.secondary.persistence.document.UserAccountDocument;
import com.teixeirah.trw.infra.secondary.persistence.repo.UserAccountMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class MongoUserAccountRepository implements UserAccountRepository {

    private final UserAccountMongoRepository repo;

    public Optional<UserAccount> find(ClientId id) {
        return repo.findById(id.value()).map(UserAccountDocument::toDomain);
    }

    @Override
    public void save(UserAccount userAccount) {
        repo.save(UserAccountDocument.fromDomain(userAccount));
    }
}



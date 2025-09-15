package com.teixeirah.trw.domain.user;

import java.util.Optional;

public interface UserAccountRepository {
  Optional<UserAccount> find(ClientId id);
  void save(UserAccount userAccount);
}



package com.teixeirah.trw.infra.secondary.persistence.adapter;

import com.teixeirah.trw.application.dto.AccountInformationForMonitoring;
import com.teixeirah.trw.application.ports.output.AccountInformationForMonitoringPort;
import com.teixeirah.trw.infra.secondary.persistence.document.PnlSnapshotDocument;
import com.teixeirah.trw.infra.secondary.persistence.document.UserAccountDocument;
import com.teixeirah.trw.infra.secondary.persistence.repo.PnlSnapshotMongoRepository;
import com.teixeirah.trw.infra.secondary.persistence.repo.UserAccountMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class MongoAccountInformationForMonitoringAdapter implements AccountInformationForMonitoringPort {

    private final UserAccountMongoRepository userRepo;
    private final PnlSnapshotMongoRepository pnlRepo;

    @Override
    public List<AccountInformationForMonitoring> fetchAccountInformationForMonitoring() {
        return userRepo.findAll().stream()
                .map(UserAccountDocument::toDomain)
                .map(userAccount -> new AccountInformationForMonitoring(
                        userAccount.clientId(),
                        userAccount.apiKey(),
                        userAccount.apiSecret(),
                        pnlRepo.findFirstByClientIdOrderByTsDesc(userAccount.clientId().value())
                                .map(PnlSnapshotDocument::toDomain)
                                .orElse(null)
                ))
                .toList();
    }
}



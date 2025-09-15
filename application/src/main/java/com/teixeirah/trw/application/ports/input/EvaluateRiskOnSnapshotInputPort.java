package com.teixeirah.trw.application.ports.input;

import com.teixeirah.trw.domain.user.ClientId;

public interface EvaluateRiskOnSnapshotInputPort {

    void handle(ClientId clientId);

}

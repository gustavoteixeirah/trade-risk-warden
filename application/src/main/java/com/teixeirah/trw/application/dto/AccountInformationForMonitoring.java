package com.teixeirah.trw.application.dto;

import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.domain.user.PnlSnapshot;

public record AccountInformationForMonitoring(ClientId clientId, String apiKey, String apiSecret,
                                              PnlSnapshot lastSnapshot) {
}



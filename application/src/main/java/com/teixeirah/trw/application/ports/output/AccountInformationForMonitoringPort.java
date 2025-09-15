package com.teixeirah.trw.application.ports.output;

import com.teixeirah.trw.application.dto.AccountInformationForMonitoring;

import java.util.List;

public interface AccountInformationForMonitoringPort {
    List<AccountInformationForMonitoring> fetchAccountInformationForMonitoring();
}

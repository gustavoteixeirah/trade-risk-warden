package com.teixeirah.trw.infra.primary.rest.mapper;

import com.teixeirah.trw.application.dto.RegisterUserCommand;
import com.teixeirah.trw.domain.risk.RiskLimits;
import com.teixeirah.trw.domain.risk.RiskThreshold;
import com.teixeirah.trw.domain.user.ClientId;
import com.teixeirah.trw.infra.bootstrap.AppProperties;
import com.teixeirah.trw.infra.primary.rest.dto.Dtos;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.Currency;

public class ApiMappers {

    public static RegisterUserCommand toCommand(Dtos.RegisterUserRequest r, AppProperties properties) {
        var tz = StringUtils.hasText(r.tz()) ? ZoneId.of(r.tz()) : ZoneId.of(properties.tz());

        var currency = Currency.getInstance(r.currency());

        var daily = new RiskThreshold(switch (r.dailyRisk().type()) {
            case ABSOLUTE -> com.teixeirah.trw.domain.risk.ThresholdType.ABSOLUTE;
            case PERCENTAGE -> com.teixeirah.trw.domain.risk.ThresholdType.PERCENTAGE;
        }, r.dailyRisk().value());

        var max = new RiskThreshold(switch (r.maxRisk().type()) {
            case ABSOLUTE -> com.teixeirah.trw.domain.risk.ThresholdType.ABSOLUTE;
            case PERCENTAGE -> com.teixeirah.trw.domain.risk.ThresholdType.PERCENTAGE;
        }, r.maxRisk().value());

        return new RegisterUserCommand(new ClientId(r.clientId()), r.apiKey(), r.apiSecret(), new RiskLimits(daily, max), tz, currency);
    }

}



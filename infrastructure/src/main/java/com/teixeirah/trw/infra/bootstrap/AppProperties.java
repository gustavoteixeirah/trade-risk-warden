package com.teixeirah.trw.infra.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "defaults")
public record AppProperties(String tz) {}



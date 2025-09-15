package com.teixeirah.trw.infra.primary.rest.common;

import com.fasterxml.jackson.databind.MapperFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

@Configuration
public class JacksonConfig {
  @Bean
  Jackson2ObjectMapperBuilderCustomizer caseInsensitiveEnums() {
    return (Jackson2ObjectMapperBuilder builder) -> builder.featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }
}



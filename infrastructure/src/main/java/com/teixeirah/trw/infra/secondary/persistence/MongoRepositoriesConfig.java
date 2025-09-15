package com.teixeirah.trw.infra.secondary.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.teixeirah.trw.infra.secondary.persistence.repo")
public class MongoRepositoriesConfig {
}



package com.epam.gym.trainerworkloadservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

@TestConfiguration
@Profile("stg")
public class TestMongoConfig {

    @Bean
    @Primary
    public MongoClient mongoClient() {
        // Connection string with authentication
        ConnectionString connectionString = new ConnectionString(
                "mongodb://admin:admin123@localhost:27017/test-workload-db?authSource=admin"
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS))
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "test-workload-db");
    }
}
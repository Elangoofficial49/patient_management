package com.patientmanagement.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoClient mongoClient() {
        try {
            log.info("Starting pure Java in-memory MongoDB server on port 27017...");
            MongoServer server = new MongoServer(new MemoryBackend());
            server.bind("localhost", 27017);
            log.info("In-memory MongoDB server bound to localhost:27017 successfully");
        } catch (Exception e) {
            log.info("Note: MongoDB server socket binding status: {}", e.getMessage());
        }
        return MongoClients.create("mongodb://localhost:27017/patient_management");
    }
}


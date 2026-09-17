package com.patientmanagement.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        if (mongoUri.contains("localhost:27017") || mongoUri.contains("127.0.0.1:27017")) {
            try {
                log.info("Starting pure Java in-memory MongoDB server on port 27017...");
                MongoServer server = new MongoServer(new MemoryBackend());
                server.bind("localhost", 27017);
                log.info("In-memory MongoDB server bound to localhost:27017 successfully");
            } catch (Exception e) {
                log.info("Note: MongoDB server socket binding status: {}", e.getMessage());
            }
        } else {
            log.info("Connecting to external MongoDB database via URI...");
        }
        return MongoClients.create(mongoUri);
    }
}



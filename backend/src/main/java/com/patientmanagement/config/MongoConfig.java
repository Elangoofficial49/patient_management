package com.patientmanagement.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.Document;
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
        if (mongoUri != null && !mongoUri.contains("localhost") && !mongoUri.contains("127.0.0.1")) {
            try {
                log.info("Attempting connection to external MongoDB database...");
                String testUri = mongoUri.contains("?") ? mongoUri + "&serverSelectionTimeoutMS=2500" : mongoUri + "?serverSelectionTimeoutMS=2500";
                MongoClient client = MongoClients.create(testUri);
                // Ping the server to test connection & IP whitelist
                client.getDatabase("admin").runCommand(new Document("ping", 1));
                log.info("Successfully connected to external MongoDB database!");
                return client;
            } catch (Exception e) {
                log.warn("External MongoDB connection ping failed ({}). Falling back to embedded in-memory MongoDB...", e.getMessage());
            }
        }

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




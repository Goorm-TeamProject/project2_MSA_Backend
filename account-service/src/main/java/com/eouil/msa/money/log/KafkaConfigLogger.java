package com.eouil.msa.money.log;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class KafkaConfigLogger {
    @Value("${spring.kafka.bootstrap-servers:NOT_SET}")
    private String kafkaServers;

    @PostConstruct
    public void printKafkaServers() {
        System.out.println("[ENV] KAFKA_BOOTSTRAP_SERVERS=" + System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        System.out.println("[YML] spring.kafka.bootstrap-servers=" + kafkaServers);
    }
}

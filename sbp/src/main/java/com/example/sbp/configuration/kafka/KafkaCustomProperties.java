package com.example.sbp.configuration.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value = "spring.kafka.topic")
@Getter
@Setter
public class KafkaCustomProperties {
	private Orders orders = new Orders();

	@Getter @Setter
	public static class Orders {
		private String name;
		private int partitions;
		private short replicas;
		private int minInsyncReplicas;
		private long retentionMs;
		private String cleanupPolicy;
	}
}

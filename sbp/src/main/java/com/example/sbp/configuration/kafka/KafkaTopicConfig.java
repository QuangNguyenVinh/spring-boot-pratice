package com.example.sbp.configuration.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
	@Bean
	public NewTopic ordersTopic(KafkaCustomProperties props) {

		var o = props.getOrders();

		return TopicBuilder.name(o.getName())
				.partitions(o.getPartitions())
				.replicas(o.getReplicas())
				.config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,
						String.valueOf(o.getMinInsyncReplicas()))
				.config(TopicConfig.RETENTION_MS_CONFIG,
						String.valueOf(o.getRetentionMs()))
				.config(TopicConfig.CLEANUP_POLICY_CONFIG,
						o.getCleanupPolicy())
				.build();
	}
}

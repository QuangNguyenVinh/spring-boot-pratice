package com.example.sbp.configuration.kafka;

import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, Object> consumerFactory(
			KafkaProperties kafkaProperties) {

		Map<String, Object> config = kafkaProperties.buildConsumerProperties();
		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			KafkaProperties kafkaProperties,
			KafkaTemplate<String, Object> kafkaTemplate) {

		var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
		factory.setConsumerFactory(consumerFactory(kafkaProperties));

		// Concurrency (must <= partitions)
		factory.setConcurrency(kafkaProperties.getListener().getConcurrency());

		factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());

		var isEnableRetry = kafkaProperties.getRetry().getTopic().isEnabled();
		if(isEnableRetry){
			// 🔥 Retry + DLQ
			DefaultErrorHandler errorHandler = getDefaultErrorHandler(kafkaProperties, kafkaTemplate);

			// Optional: retry only certain exceptions
			errorHandler.addRetryableExceptions(RuntimeException.class);
			errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
			factory.setCommonErrorHandler(errorHandler);
		}

		return factory;
	}

	private static @NonNull DefaultErrorHandler getDefaultErrorHandler(KafkaProperties kafkaProperties, KafkaTemplate<String, Object> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer =
				new DeadLetterPublishingRecoverer(kafkaTemplate);
		var retryConfig = kafkaProperties.getRetry().getTopic();
		// Retry 3 times, 2 seconds backoff
		ExponentialBackOff backOff = new ExponentialBackOff();
		backOff.setMaxAttempts(retryConfig.getAttempts());
		backOff.setInitialInterval(retryConfig.getBackoff().getDelay().toMillis());
		backOff.setMultiplier(retryConfig.getBackoff().getMultiplier());

		DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
		return errorHandler;
	}
}

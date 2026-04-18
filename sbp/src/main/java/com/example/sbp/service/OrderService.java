package com.example.sbp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {


	@KafkaListener(topics = "${spring.kafka.topic.orders.name}")
	public void consume(String message, Acknowledgment ack) {

		try {
			if (message.contains("fail")) {
				throw new RuntimeException("Simulated failure");
			}

			log.info("Processing...");

			// process
			ack.acknowledge();

		} catch (Exception ex) {
			log.error("Failed to process message due to {}", ex.getLocalizedMessage());
			throw ex;
		}
	}

}

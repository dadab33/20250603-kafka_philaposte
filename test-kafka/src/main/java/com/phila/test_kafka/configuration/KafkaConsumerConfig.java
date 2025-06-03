package com.phila.test_kafka.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.phila.test_kafka.beans.Facture;
import com.phila.test_kafka.beans.FactureKey;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServer;
	
	@Bean
	public ConsumerFactory<FactureKey, Facture> consumerFactory(MeterRegistry meterRegistry) {
		Map<String, Object> props = new HashMap<>();
		
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		
		var jsonDeserializer = new JsonDeserializer<Facture>();
		jsonDeserializer.trustedPackages("*");
		
		var jsonDeserializerKey = new JsonDeserializer<FactureKey>();
		jsonDeserializerKey.trustedPackages("*");
		
		var factory = new DefaultKafkaConsumerFactory<FactureKey, Facture>(props, jsonDeserializerKey, jsonDeserializer);
		factory.addListener(new MicrometerConsumerListener(meterRegistry));
		return factory;
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<FactureKey, Facture> kafkaListenerContainerFactory(ConsumerFactory<FactureKey, Facture> consumerFactory) {
		var factory = new ConcurrentKafkaListenerContainerFactory<FactureKey, Facture>();
		factory.setConsumerFactory(consumerFactory);
		factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
		return factory;
	}
}

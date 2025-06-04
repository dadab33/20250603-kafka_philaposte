package com.phila.test_kafka.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.phila.test_kafka.beans.Facture;
import com.phila.test_kafka.beans.FactureKey;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServer;
		@Bean
	public ProducerFactory<FactureKey, Facture> producerFactory(MeterRegistry meterRegistry) {
		Map<String, Object> props = new HashMap<>();
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		
		// Configuration du partitionneur personnalisé
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, ConsommateurPartitioner.class);
		
		var pf = new DefaultKafkaProducerFactory<FactureKey, Facture>(props);
		pf.addListener(new MicrometerProducerListener<>(meterRegistry));
		return pf;
	}
	
	@Bean
	public KafkaTemplate<FactureKey, Facture> kafkaTemplate(ProducerFactory<FactureKey, Facture> producerFactory) {
		return new KafkaTemplate<FactureKey, Facture>(producerFactory);
	}
}

package com.phila.test_kafka.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.phila.test_kafka.beans.Facture;
import com.phila.test_kafka.beans.FactureKey;

@Service
public class KafkaMessageProducer {

	@Value("${spring.kafka.topics}")
	private String topic;
	
	@Autowired
	private KafkaTemplate<FactureKey, Facture> kafkaTemplate;
	
	public void sendMessage(Facture facture, String consommateur) {
		System.out.println("Envoi de la facture : " + facture.toString());
		
		kafkaTemplate.send(topic, new FactureKey(consommateur, UUID.randomUUID().toString()) , facture)
		.whenComplete((result, ex) -> {
			if(ex == null) {
				System.out.printf("Succès ! Topic : %s, Partition: %d, Offset: %d%n", 
						result.getRecordMetadata().topic(), 
						result.getRecordMetadata().partition(), 
						result.getRecordMetadata().offset());
			} else {
				System.err.println("Erreur durant l'envoi");
			}
		});
	}
}

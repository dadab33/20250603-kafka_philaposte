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
		public void sendMessage(Facture facture, String consommateur, int idConso) {
		System.out.println("Envoi de la facture : " + facture.toString() + " pour le consommateur : " + consommateur);
		
		FactureKey key = new FactureKey(consommateur, idConso);
		
		kafkaTemplate.send(topic, key, facture)
		.whenComplete((result, ex) -> {
			if(ex == null) {
				System.out.printf("Succès ! Consommateur: %s, Topic: %s, Partition: %d, Offset: %d%n", 
						consommateur,
						result.getRecordMetadata().topic(), 
						result.getRecordMetadata().partition(), 
						result.getRecordMetadata().offset());
			} else {
				System.err.println("Erreur durant l'envoi pour le consommateur " + consommateur + " : " + ex.getMessage());
			}
		});
	}
}

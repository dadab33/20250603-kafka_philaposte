package com.phila.test_kafka.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.phila.test_kafka.beans.Facture;
import com.phila.test_kafka.beans.FactureKey;

@Component
public class KafkaMessageConsumer {	
	@KafkaListener(topics= "#{'${spring.kafka.topics}'}", groupId="phila-consumer-group", containerFactory = "kafkaListenerContainerFactory") 
	public void listen (ConsumerRecord<FactureKey, Facture> record, Acknowledgment acknowledgment) {
		//System.out.println("Message reçu : Key => " + record.key().toString() + " / Value => " + record.value().toString());
		acknowledgment.acknowledge();
		//System.out.println("Message commité pour le groupId phila-consumer-group");
	}
}

package com.phila.test_kafka.controllers;

import java.io.Console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.phila.test_kafka.beans.Facture;
import com.phila.test_kafka.services.KafkaMessageProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController(value = "/kafka")
public class KafkaTestController {

	@Autowired
	private KafkaMessageProducer kafkaMessageProducer;
	
	@GetMapping("/produce")
	public void produce (@RequestParam String numFacture, @RequestParam String nomFacture, @RequestParam String consommateur) {
		for(int i = 0; i < 10000; i++)
			kafkaMessageProducer.sendMessage(new Facture(numFacture, nomFacture + " " + i), consommateur);
	}
}

package com.phila.test_kafka.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Facture {
	private String numFacture;
	private String nomFacture;
}

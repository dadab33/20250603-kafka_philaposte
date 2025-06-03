package com.phila.test_kafka.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactureKey {
	public String consommateur;
	public String id;
}

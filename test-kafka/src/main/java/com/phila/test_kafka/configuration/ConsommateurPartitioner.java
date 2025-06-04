package com.phila.test_kafka.configuration;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phila.test_kafka.beans.FactureKey;

/**
 * Partitionneur personnalisé qui route les messages vers des partitions
 * en fonction du champ 'consommateur' de la FactureKey.
 * 
 * Ce partitionneur garantit que tous les messages d'un même consommateur
 * seront toujours envoyés vers la même partition, assurant ainsi l'ordre
 * des messages par consommateur.
 */
@Component
public class ConsommateurPartitioner implements Partitioner {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // Récupérer le nombre de partitions pour ce topic
        int numPartitions = cluster.partitionCountForTopic(topic);
        
        if (key == null) {
            // Si pas de clé, utiliser le partitionnement round-robin par défaut
            return Utils.toPositive(Utils.murmur2(valueBytes)) % numPartitions;
        }
        
        try {
            // Si la clé est déjà un objet FactureKey
            if (key instanceof FactureKey) {
                FactureKey factureKey = (FactureKey) key;
                return getPartitionForConsommateur(factureKey.getConsommateur(), numPartitions);
            }
            
            // Si la clé est sérialisée en JSON, la désérialiser
            if (keyBytes != null) {
                FactureKey factureKey = objectMapper.readValue(keyBytes, FactureKey.class);
                return getPartitionForConsommateur(factureKey.getConsommateur(), numPartitions);
            }
            
        } catch (Exception e) {
            // En cas d'erreur de désérialisation, utiliser le partitionnement par défaut
            System.err.println("Erreur lors du partitionnement : " + e.getMessage());
        }
        
        // Fallback : partitionnement basé sur le hashcode de la clé
        return Utils.toPositive(key.hashCode()) % numPartitions;
    }

    /**
     * Calcule la partition pour un consommateur donné.
     * Utilise un hash consistant pour garantir que le même consommateur
     * va toujours vers la même partition.
     */
    private int getPartitionForConsommateur(String consommateur, int numPartitions) {
        if (consommateur == null || consommateur.trim().isEmpty()) {
            return 0; // Partition par défaut si consommateur vide
        }
        
        // Utiliser un hash consistant pour garantir la même partition pour le même consommateur
        return Utils.toPositive(Utils.murmur2(consommateur.getBytes())) % numPartitions;
    }

    @Override
    public void close() {
        // Rien à nettoyer
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Aucune configuration nécessaire
    }
}

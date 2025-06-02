1) Etape 1 :
Spring initializr : Spring Web / Spring Kafka / Lomgbok / Actuator

// Modif dans le src/main/resources/application.properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=my_spring_consumer_group
spring.kafka.consumer.auto-offset-reset=earliest

// Producer
@Service
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final static String TOPIC_NAME = "my_spring_topic";

    // Spring injecte automatiquement le KafkaTemplate configuré
    @Autowired
    public KafkaMessageProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        System.out.println("Envoi du message: " + message + " vers le sujet: " + TOPIC_NAME);

        // Envoyer le message avec une clé et une valeur
        // Le `send` retourne un `ListenableFuture` pour gérer le résultat de manière asynchrone
        kafkaTemplate.send(TOPIC_NAME, "key-" + System.currentTimeMillis(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        System.out.printf("Message envoyé avec succès ! Topic: %s, Partition: %d, Offset: %d%n",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        System.err.println("Erreur lors de l'envoi du message: " + ex.getMessage());
                    }
                });
    }
}

// Création du consumer
@Component
public class KafkaMessageConsumer {

    private final static String TOPIC_NAME = "my_spring_topic";
    private final static String GROUP_ID = "my_spring_consumer_group";

    @KafkaListener(topics = TOPIC_NAME, groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message, Acknowledgment acknowledgment) {
        System.out.println("Message reçu par le consommateur Spring -> " + message);
        acknowledgment.acknowledge();
        System.out.println("Offset committé pour le message: " + message);
    }
}

@Configuration
public class KafkaConsumerConfig {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Bean
	public ConsumerFactory<String, Personne> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
	    JsonDeserializer<Personne> payloadJsonDeserializer = new JsonDeserializer();
	    payloadJsonDeserializer.trustedPackages("*");
        props.put("auto.offset.reset", "earliest"); // Lire depuis le début

		return new DefaultKafkaConsumerFactory<String, Personne>(props, new StringDeserializer(), payloadJsonDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
		return factory;
	}
}

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Personne> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Personne> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}


// Gérer ensuite la sérialisation / désérialisation en JSON

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

Création d'un objet Personne

// Gestion des Actuator
# Configuration Actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
management.endpoint.metrics.enabled=true
management.endpoint.prometheus.enabled=true

# Configuration des métriques Kafka
spring.kafka.template.observation-enabled=true
spring.kafka.listener.observation-enabled=true

# Configuration additionnelle pour les métriques
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.percentiles-histogram.kafka.consumer=true
management.metrics.distribution.percentiles-histogram.kafka.producer=true

Modif des config :
    public ProducerFactory<String, Personne> producerFactory(MeterRegistry meterRegistry) {

Et le return :
        var factory = new DefaultKafkaProducerFactory<String, Personne>(configProps);
        
        // Ajouter le listener Micrometer pour capturer les métriques
        factory.addListener(new MicrometerProducerListener<>(meterRegistry));
        
        return factory;

    public KafkaTemplate<String, Personne> kafkaTemplate(ProducerFactory<String, Personne> producerFactory) {
        return new KafkaTemplate<>(producerFactory);


	public ConsumerFactory<String, Personne> consumerFactory(MeterRegistry meterRegistry) {
		
		var factory = new DefaultKafkaConsumerFactory<String, Personne>(props, new StringDeserializer(), payloadJsonDeserializer);
		
		// Ajouter le listener Micrometer pour capturer les métriques
		factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
		
		return factory;

	public ConcurrentKafkaListenerContainerFactory<String, Personne> kafkaListenerContainerFactory(
			ConsumerFactory<String, Personne> consumerFactory) {

		factory.setConsumerFactory(consumerFactory);


// Faire une partie sur la DLQ

package com.travelbooking.testutils.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Component
public class TestConsumer {

  @Autowired(required = false)
  private EmbeddedKafkaBroker embeddedKafkaBroker;
  
  private String bootstrapServers;

  public TestConsumer() {
    // Default constructor for Spring component scanning
  }
  
  public TestConsumer(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  public <K, V> TestSubscription<K, V> subscribe(String topic, Deserializer<K> keyDeserializer,
                                                 @Nullable Deserializer<V> valueDeserializer) {
    String uniqueGroupId = "test-group-" + UUID.randomUUID();
    Map<String, Object> consumerProps;
    
    if (bootstrapServers != null) {
      // Using KafkaContainer or external Kafka
      consumerProps = new java.util.HashMap<>();
      consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, uniqueGroupId);
      consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
      consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
    } else if (embeddedKafkaBroker != null) {
      // Using EmbeddedKafkaBroker  
      consumerProps = KafkaTestUtils.consumerProps(uniqueGroupId, "true", embeddedKafkaBroker);
    } else {
      throw new IllegalStateException("Neither bootstrapServers nor embeddedKafkaBroker is configured");
    }
    
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
    var consumer = new DefaultKafkaConsumerFactory<>(consumerProps, keyDeserializer, valueDeserializer)
        .createConsumer();
    consumer.subscribe(Collections.singletonList(topic));
    return new TestSubscription<>(consumer, topic);
  }

  public TestSubscription<String, String> subscribe(String topic) {
    return subscribe(topic, new StringDeserializer(),new StringDeserializer());
  }

  public <V> TestSubscription<String, V> subscribeForJSon(String topic, Class<V> messageClass) {
    String uniqueGroupId = "test-group-" + UUID.randomUUID();
    Map<String, Object> consumerProps;
    
    if (bootstrapServers != null) {
      // Using KafkaContainer or external Kafka
      consumerProps = new java.util.HashMap<>();
      consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, uniqueGroupId);
      consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
      consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
    } else if (embeddedKafkaBroker != null) {
      // Using EmbeddedKafkaBroker
      consumerProps = KafkaTestUtils.consumerProps(uniqueGroupId, "true", embeddedKafkaBroker);
    } else {
      throw new IllegalStateException("Neither bootstrapServers nor embeddedKafkaBroker is configured");
    }
    
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.travelbooking.*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, messageClass);

    var consumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new JsonDeserializer<>(messageClass))
        .createConsumer();
    consumer.subscribe(Collections.singletonList(topic));
    return new TestSubscription<>(consumer, topic);
  }
}

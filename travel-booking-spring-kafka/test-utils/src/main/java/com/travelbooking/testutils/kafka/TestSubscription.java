package com.travelbooking.testutils.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public record TestSubscription<K, V>(Consumer<K, V> consumer, String topic) implements AutoCloseable {
  
  private static final Logger logger = LoggerFactory.getLogger(TestSubscription.class);

  public static void closeQuietly(TestSubscription<?, ?> subscription) {
    if (subscription != null) {
      subscription.close();
    }
  }

  public void close() {
    consumer.close();
  }

  public void assertRecordReceived(java.util.function.Consumer<ConsumerRecord<K, V>> recordConsumer) {
    List<AssertionError> allErrors = new java.util.ArrayList<>();
    Awaitility.await().atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(500));
          logger.debug("Polled {} records from topic: {}", records.count(), topic);
          List<AssertionError> errors = new java.util.ArrayList<>();
          for (ConsumerRecord<K, V> record : records) {
            try {
              recordConsumer.accept(record);
              return;
            } catch (AssertionError e) {
              errors.add(e);
              allErrors.add(e);
            }
          }
          if (!errors.isEmpty()) {
            throw new MultiAssertionError("Last - No matching record found in topic: " + topic, errors);
          } else if (!allErrors.isEmpty()) {
            throw new MultiAssertionError("All - No matching record found in topic: " + topic, allErrors);
          } else {
            org.assertj.core.api.Assertions.fail("No records received from topic: " + topic);
          }
        });
  }
}

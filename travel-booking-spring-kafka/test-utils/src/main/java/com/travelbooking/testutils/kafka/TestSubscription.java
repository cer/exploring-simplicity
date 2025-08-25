package com.travelbooking.testutils.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.List;

public record TestSubscription<K, V>(Consumer<K, V> consumer) {

  public static void closeQuietly(TestSubscription<?, ?> subscription) {
    if (subscription != null) {
      subscription.close();
    }
  }

  public void close() {
    consumer.close();
  }

  public void assertRecordReceived(java.util.function.Consumer<ConsumerRecord<K, V>> recordConsumer) {
    Awaitility.await().atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          ConsumerRecords<K, V> records = consumer.poll(Duration.ofMillis(500));
          System.out.println("**** Polled " + records.count() + " records");
          List<AssertionError> errors = new java.util.ArrayList<>();
          for (ConsumerRecord<K, V> record : records) {
            try {
              recordConsumer.accept(record);
              return;
            } catch (AssertionError e) {
              errors.add(e);
            }
          }
          if (errors.isEmpty()) {
            org.assertj.core.api.Assertions.fail("No records received");
          } else {
            throw new AssertionError("No matching record found", errors.getFirst());
          }
        });
  }
}

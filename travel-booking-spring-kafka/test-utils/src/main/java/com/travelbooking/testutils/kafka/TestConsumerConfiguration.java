package com.travelbooking.testutils.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConsumerConfiguration {
  @Bean
  public TestConsumer testConsumer() {
    return new TestConsumer();
  }

}

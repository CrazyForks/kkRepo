package com.github.klboke.kkrepo.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryCacheConfiguration {
  @Bean
  @ConditionalOnMissingBean(SharedCache.class)
  @ConditionalOnProperty(
      prefix = "kkrepo.cache",
      name = "backend",
      havingValue = "memory",
      matchIfMissing = true)
  SharedCache inMemorySharedCache(
      ObjectMapper objectMapper,
      @Value("${kkrepo.cache.memory.maximum-size:500000}") long maximumSize,
      ObjectProvider<MeterRegistry> meterRegistry) {
    return new InMemorySharedCache(objectMapper, maximumSize, meterRegistry.getIfAvailable());
  }
}

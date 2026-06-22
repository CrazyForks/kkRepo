package com.github.klboke.kkrepo.server.security;

import com.github.klboke.kkrepo.core.security.EncryptionSecrets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Bridges the encryption secrets declared in the application config file
 * ({@code kkrepo.security.encryption.*}) into the Spring-agnostic {@link EncryptionSecrets}
 * holder. Running as a constructor of an eagerly-created {@code @Configuration} bean, this completes
 * during context startup — before any request-time code resolves a secret — so config-file values
 * take precedence over the system-property / environment fallbacks.
 */
@Configuration
public class EncryptionSecretsConfigurer {
  public EncryptionSecretsConfigurer(
      @Value("${kkrepo.security.encryption.credential-secret:}") String credentialSecret,
      @Value("${kkrepo.security.encryption.api-key-payload-secret:}") String apiKeyPayloadSecret) {
    EncryptionSecrets.configure(credentialSecret, apiKeyPayloadSecret);
  }
}

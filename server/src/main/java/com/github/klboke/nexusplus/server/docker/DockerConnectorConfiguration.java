package com.github.klboke.nexusplus.server.docker;

import com.github.klboke.nexusplus.core.RepositoryFormat;
import com.github.klboke.nexusplus.persistence.mysql.dao.RepositoryDao;
import com.github.klboke.nexusplus.persistence.mysql.model.RepositoryRecord;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerConnectorConfiguration {
  public static final String CONNECTOR_REPOSITORY_ATTRIBUTE =
      DockerConnectorConfiguration.class.getName() + ".REPOSITORY";

  @Bean
  WebServerFactoryCustomizer<TomcatServletWebServerFactory> dockerConnectorCustomizer(
      RepositoryDao repositoryDao,
      @Value("${nexus-plus.docker.connector.enabled:true}") boolean enabled) {
    return factory -> {
      if (!enabled) {
        return;
      }
      for (Map.Entry<Integer, String> entry : connectorPorts(repositoryDao).entrySet()) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(entry.getKey());
        connector.setProperty("connectionTimeout", "60000");
        connector.setProperty("maxConnections", "2000");
        factory.addAdditionalConnectors(connector);
      }
    };
  }

  @Bean
  Filter dockerConnectorRepositoryFilter(ObjectProvider<RepositoryDao> repositoryDaoProvider) {
    return new Filter() {
      private volatile Map<Integer, String> ports;

      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
        if (request instanceof HttpServletRequest http) {
          String repository = currentPorts(repositoryDaoProvider).get(http.getLocalPort());
          if (repository != null && http.getRequestURI().startsWith("/v2/")) {
            http.setAttribute(CONNECTOR_REPOSITORY_ATTRIBUTE, repository);
          }
        }
        chain.doFilter(request, response);
      }

      private Map<Integer, String> currentPorts(ObjectProvider<RepositoryDao> provider) {
        Map<Integer, String> local = ports;
        if (local != null) {
          return local;
        }
        RepositoryDao dao = provider.getIfAvailable();
        local = dao == null ? Map.of() : connectorPorts(dao);
        ports = local;
        return local;
      }
    };
  }

  static Map<Integer, String> connectorPorts(RepositoryDao repositoryDao) {
    Map<Integer, String> ports = new LinkedHashMap<>();
    for (RepositoryRecord record : repositoryDao.list()) {
      if (record.format() != RepositoryFormat.DOCKER || record.attributes() == null) {
        continue;
      }
      Object raw = record.attributes().get("docker");
      if (!(raw instanceof Map<?, ?> docker)) {
        continue;
      }
      Object enabled = docker.get("connectorEnabled");
      Object port = docker.get("connectorPort");
      if (port == null || (enabled instanceof Boolean b && !b)) {
        continue;
      }
      Integer parsed = parsePort(port);
      if (parsed != null) {
        ports.put(parsed, record.name());
      }
    }
    return Map.copyOf(ports);
  }

  private static Integer parsePort(Object value) {
    if (value instanceof Number number) {
      return number.intValue();
    }
    try {
      return Integer.parseInt(value.toString());
    } catch (RuntimeException ignored) {
      return null;
    }
  }
}

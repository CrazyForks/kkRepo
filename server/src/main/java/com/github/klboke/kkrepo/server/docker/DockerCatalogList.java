package com.github.klboke.kkrepo.server.docker;

import java.util.List;
import java.util.Map;

public record DockerCatalogList(List<String> repositories, boolean hasNext) {
  public DockerCatalogList {
    repositories = repositories == null ? List.of() : List.copyOf(repositories);
  }

  public Map<String, Object> body() {
    return Map.of("repositories", repositories);
  }
}

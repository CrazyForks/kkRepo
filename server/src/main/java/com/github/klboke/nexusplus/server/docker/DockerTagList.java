package com.github.klboke.nexusplus.server.docker;

import java.util.List;
import java.util.Map;

public record DockerTagList(String imageName, List<String> tags, boolean hasNext) {
  public Map<String, Object> body() {
    return Map.of("name", imageName, "tags", tags == null ? List.of() : tags);
  }
}

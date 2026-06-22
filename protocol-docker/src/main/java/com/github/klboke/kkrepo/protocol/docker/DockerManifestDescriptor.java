package com.github.klboke.kkrepo.protocol.docker;

import java.util.Map;

public record DockerManifestDescriptor(
    String kind,
    String digest,
    String mediaType,
    Long size,
    Map<String, Object> platform,
    Map<String, Object> annotations) {
}

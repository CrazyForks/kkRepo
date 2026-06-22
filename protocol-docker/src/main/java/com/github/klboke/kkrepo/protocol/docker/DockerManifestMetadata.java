package com.github.klboke.kkrepo.protocol.docker;

import java.util.List;

public record DockerManifestMetadata(
    String mediaType,
    String artifactType,
    String subjectDigest,
    List<DockerManifestDescriptor> references) {
}

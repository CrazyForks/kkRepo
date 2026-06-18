package com.github.klboke.nexusplus.protocol.docker;

import java.util.List;

public record DockerManifestMetadata(
    String mediaType,
    String artifactType,
    String subjectDigest,
    List<DockerManifestDescriptor> references) {
}

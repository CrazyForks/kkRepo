package com.github.klboke.kkrepo.core;

import java.time.Instant;

public record BlobObjectMetadata(
    BlobReference reference,
    String eTag,
    String contentType,
    Instant lastModified) {
}

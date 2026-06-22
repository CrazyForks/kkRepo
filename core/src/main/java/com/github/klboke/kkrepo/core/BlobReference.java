package com.github.klboke.kkrepo.core;

public record BlobReference(
    String bucket,
    String objectKey,
    String sha256,
    long size) {
}

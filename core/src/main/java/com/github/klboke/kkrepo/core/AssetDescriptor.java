package com.github.klboke.kkrepo.core;

public record AssetDescriptor(
    String repository,
    String logicalPath,
    PackageCoordinate coordinate,
    BlobReference blob,
    String contentType) {
}

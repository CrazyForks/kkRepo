package com.github.klboke.kkrepo.core;

public record PackageCoordinate(
    RepositoryFormat format,
    String namespace,
    String name,
    String version) {
}

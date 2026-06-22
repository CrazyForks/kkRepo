package com.github.klboke.kkrepo.core;

public record RepositoryDescriptor(
    String name,
    RepositoryFormat format,
    RepositoryType type,
    boolean online) {
}

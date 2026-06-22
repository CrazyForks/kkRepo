package com.github.klboke.kkrepo.core;

/**
 * Names a (format, type) pair that kkrepo knows how to host. The {@code name} mirrors
 * the strings Nexus uses on the wire so that compat-test responses match without translation.
 */
public record RepositoryRecipe(String name, RepositoryFormat format, RepositoryType type) {
}

package com.github.klboke.kkrepo.persistence.mysql.model;

public record SecurityAnonymousConfigRecord(
    boolean enabled,
    String userSource,
    String userId,
    String realmName) {
}

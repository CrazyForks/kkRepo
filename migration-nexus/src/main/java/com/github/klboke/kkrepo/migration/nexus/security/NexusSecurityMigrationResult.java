package com.github.klboke.kkrepo.migration.nexus.security;

public record NexusSecurityMigrationResult(
    int contentSelectors,
    int privileges,
    int roles,
    int users,
    int userRoleMappings,
    int realms,
    int apiKeys,
    int anonymousConfigs) {

  public static NexusSecurityMigrationResult empty() {
    return new NexusSecurityMigrationResult(0, 0, 0, 0, 0, 0, 0, 0);
  }
}

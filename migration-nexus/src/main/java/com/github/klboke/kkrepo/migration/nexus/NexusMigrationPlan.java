package com.github.klboke.kkrepo.migration.nexus;

import java.nio.file.Path;

public record NexusMigrationPlan(
    Path nexusDataDirectory,
    boolean migrateProxyArtifacts,
    boolean dryRun) {
}

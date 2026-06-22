package com.github.klboke.kkrepo.protocol.maven.policy;

public enum VersionPolicy {
  RELEASE,
  SNAPSHOT,
  MIXED;

  public static VersionPolicy parse(String value) {
    if (value == null || value.isBlank()) return MIXED;
    return VersionPolicy.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
  }
}

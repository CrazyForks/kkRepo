package com.github.klboke.nexusplus.protocol.docker;

import java.util.Collection;
import java.util.Locale;

public final class DockerManifestAccept {
  private DockerManifestAccept() {
  }

  public static boolean accepts(Collection<String> acceptHeaders, String mediaType) {
    if (mediaType == null || mediaType.isBlank()) {
      return true;
    }
    if (acceptHeaders == null || acceptHeaders.isEmpty()) {
      return true;
    }
    String stored = normalize(mediaType);
    boolean sawAccept = false;
    for (String header : acceptHeaders) {
      if (header == null || header.isBlank()) {
        continue;
      }
      for (String part : header.split(",")) {
        String accepted = normalize(part);
        if (accepted.isBlank()) {
          continue;
        }
        sawAccept = true;
        if ("*/*".equals(accepted) || accepted.equals(stored) || wildcardMatches(accepted, stored)) {
          return true;
        }
      }
    }
    return !sawAccept;
  }

  private static boolean wildcardMatches(String accepted, String stored) {
    int slash = accepted.indexOf('/');
    if (slash <= 0 || slash == accepted.length() - 1 || !accepted.endsWith("/*")) {
      return false;
    }
    return stored.startsWith(accepted.substring(0, slash + 1));
  }

  private static String normalize(String value) {
    String normalized = value == null ? "" : value.trim();
    int semicolon = normalized.indexOf(';');
    if (semicolon >= 0) {
      normalized = normalized.substring(0, semicolon).trim();
    }
    return normalized.toLowerCase(Locale.ROOT);
  }
}

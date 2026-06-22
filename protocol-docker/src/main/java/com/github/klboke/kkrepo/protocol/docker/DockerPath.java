package com.github.klboke.kkrepo.protocol.docker;

public record DockerPath(
    Kind kind,
    String imageName,
    String reference,
    DockerDigest digest,
    String uploadUuid) {
  public enum Kind {
    BASE,
    MANIFEST,
    BLOB,
    UPLOAD_START,
    UPLOAD_SESSION,
    TAGS,
    REFERRERS,
    CATALOG
  }
}

package com.github.klboke.kkrepo.protocol.docker;

public enum DockerErrorCode {
  BLOB_UNKNOWN("blob unknown to registry"),
  BLOB_UPLOAD_INVALID("blob upload invalid"),
  BLOB_UPLOAD_UNKNOWN("blob upload unknown to registry"),
  DIGEST_INVALID("provided digest did not match uploaded content"),
  MANIFEST_BLOB_UNKNOWN("manifest references a manifest or blob unknown to registry"),
  MANIFEST_INVALID("manifest invalid"),
  MANIFEST_UNKNOWN("manifest unknown"),
  NAME_INVALID("invalid repository name"),
  NAME_UNKNOWN("repository name not known to registry"),
  SIZE_INVALID("provided length did not match content length"),
  TAG_INVALID("manifest tag did not match URI"),
  UNAUTHORIZED("authentication required"),
  DENIED("requested access to the resource is denied"),
  UNSUPPORTED("operation is unsupported");

  private final String defaultMessage;

  DockerErrorCode(String defaultMessage) {
    this.defaultMessage = defaultMessage;
  }

  public String defaultMessage() {
    return defaultMessage;
  }
}

package com.github.klboke.nexusplus.server.docker;

import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DockerResponse {
  private final int status;
  private final BodySupplier bodySupplier;
  private final long contentLength;
  private final String contentType;
  private final Instant lastModified;
  private final Map<String, String> headers;

  private DockerResponse(
      int status,
      BodySupplier bodySupplier,
      long contentLength,
      String contentType,
      Instant lastModified,
      Map<String, String> headers) {
    this.status = status;
    this.bodySupplier = bodySupplier;
    this.contentLength = contentLength;
    this.contentType = contentType;
    this.lastModified = lastModified;
    this.headers = headers;
  }

  public static DockerResponse body(
      int status,
      BodySupplier bodySupplier,
      long contentLength,
      String contentType,
      Instant lastModified) {
    return new DockerResponse(status, bodySupplier, contentLength, contentType, lastModified, new LinkedHashMap<>());
  }

  public static DockerResponse noBody(int status) {
    return new DockerResponse(status, null, -1, null, null, new LinkedHashMap<>());
  }

  public static DockerResponse noBody(
      int status,
      long contentLength,
      String contentType,
      Instant lastModified) {
    return new DockerResponse(status, null, contentLength, contentType, lastModified, new LinkedHashMap<>());
  }

  public DockerResponse withHeader(String name, String value) {
    if (name != null && value != null) {
      headers.put(name, value);
    }
    return this;
  }

  public DockerResponse withContentType(String contentType) {
    return new DockerResponse(status, bodySupplier, contentLength, contentType, lastModified, headers);
  }

  public DockerResponse withStatus(int status) {
    return new DockerResponse(status, bodySupplier, contentLength, contentType, lastModified, headers);
  }

  public int status() {
    return status;
  }

  public boolean hasBody() {
    return bodySupplier != null;
  }

  public InputStream body() {
    return bodySupplier == null ? null : bodySupplier.open();
  }

  public long contentLength() {
    return contentLength;
  }

  public String contentType() {
    return contentType;
  }

  public Instant lastModified() {
    return lastModified;
  }

  public Map<String, String> headers() {
    return headers;
  }

  @FunctionalInterface
  public interface BodySupplier {
    InputStream open();
  }
}

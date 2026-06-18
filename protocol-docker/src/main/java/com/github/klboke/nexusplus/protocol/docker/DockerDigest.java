package com.github.klboke.nexusplus.protocol.docker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public record DockerDigest(String algorithm, String hex) {
  private static final Pattern ALGORITHM = Pattern.compile("[A-Za-z][A-Za-z0-9_+.-]*");
  private static final Pattern HEX = Pattern.compile("[a-fA-F0-9]{32,}");

  public DockerDigest {
    algorithm = normalizeAlgorithm(algorithm);
    hex = normalizeHex(hex);
  }

  public static DockerDigest parse(String value) {
    if (value == null || value.isBlank()) {
      throw new DockerProtocolException(DockerErrorCode.DIGEST_INVALID, "digest is required");
    }
    int colon = value.indexOf(':');
    if (colon <= 0 || colon == value.length() - 1) {
      throw new DockerProtocolException(DockerErrorCode.DIGEST_INVALID, "invalid digest: " + value);
    }
    return new DockerDigest(value.substring(0, colon), value.substring(colon + 1));
  }

  public static DockerDigest sha256(byte[] bytes) {
    return new DockerDigest("sha256", hex(digest("SHA-256", bytes)));
  }

  public static String sha256Hex(byte[] bytes) {
    return sha256(bytes).hex();
  }

  public String value() {
    return algorithm + ":" + hex;
  }

  public boolean isSha256() {
    return "sha256".equals(algorithm);
  }

  private static String normalizeAlgorithm(String value) {
    String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (!ALGORITHM.matcher(normalized).matches()) {
      throw new DockerProtocolException(DockerErrorCode.DIGEST_INVALID, "invalid digest algorithm");
    }
    return normalized;
  }

  private static String normalizeHex(String value) {
    String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    if (!HEX.matcher(normalized).matches()) {
      throw new DockerProtocolException(DockerErrorCode.DIGEST_INVALID, "invalid digest hex");
    }
    return normalized;
  }

  private static byte[] digest(String algorithm, byte[] bytes) {
    try {
      return MessageDigest.getInstance(algorithm).digest(bytes == null ? new byte[0] : bytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(algorithm + " is unavailable", e);
    }
  }

  private static String hex(byte[] bytes) {
    return HexFormat.of().formatHex(bytes);
  }

  @Override
  public String toString() {
    return value();
  }
}

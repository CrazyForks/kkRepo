package com.github.klboke.kkrepo.protocol.cargo;

import java.math.BigInteger;
import java.util.regex.Pattern;

public final class CargoVersions {
  private static final String NUMERIC_IDENTIFIER = "0|[1-9]\\d*";
  private static final String PRERELEASE_IDENTIFIER =
      "(?:0|[1-9]\\d*|\\d*[A-Za-z-][0-9A-Za-z-]*)";
  private static final Pattern SEMVER = Pattern.compile(
      "^(" + NUMERIC_IDENTIFIER + ")\\.(" + NUMERIC_IDENTIFIER + ")\\.(" + NUMERIC_IDENTIFIER + ")"
          + "(?:-(" + PRERELEASE_IDENTIFIER + "(?:\\." + PRERELEASE_IDENTIFIER + ")*))?"
          + "(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?$");

  private CargoVersions() {
  }

  public static String uniquenessKey(String version) {
    if (version == null) {
      return null;
    }
    String trimmed = version.trim();
    int build = trimmed.indexOf('+');
    return build < 0 ? trimmed : trimmed.substring(0, build);
  }

  public static String requireVersion(String version) {
    String value = version == null ? "" : version.trim();
    if (value.isBlank()
        || value.contains("/")
        || value.contains("\\")
        || value.contains("..")
        || !SEMVER.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid Cargo crate version: " + version);
    }
    return value;
  }

  public static int compare(String left, String right) {
    String a = requireVersion(left);
    String b = requireVersion(right);
    ParsedVersion parsedLeft = ParsedVersion.parse(a);
    ParsedVersion parsedRight = ParsedVersion.parse(b);
    int core = parsedLeft.compareCore(parsedRight);
    if (core != 0) {
      return core;
    }
    return comparePreRelease(parsedLeft.preRelease(), parsedRight.preRelease());
  }

  private static int comparePreRelease(String left, String right) {
    if (left == null && right == null) {
      return 0;
    }
    if (left == null) {
      return 1;
    }
    if (right == null) {
      return -1;
    }
    String[] leftParts = left.split("\\.");
    String[] rightParts = right.split("\\.");
    int max = Math.max(leftParts.length, rightParts.length);
    for (int i = 0; i < max; i++) {
      if (i >= leftParts.length) {
        return -1;
      }
      if (i >= rightParts.length) {
        return 1;
      }
      int compared = comparePreReleaseIdentifier(leftParts[i], rightParts[i]);
      if (compared != 0) {
        return compared;
      }
    }
    return 0;
  }

  private static int comparePreReleaseIdentifier(String left, String right) {
    boolean leftNumeric = isNumeric(left);
    boolean rightNumeric = isNumeric(right);
    if (leftNumeric && rightNumeric) {
      return new BigInteger(left).compareTo(new BigInteger(right));
    }
    if (leftNumeric) {
      return -1;
    }
    if (rightNumeric) {
      return 1;
    }
    return left.compareTo(right);
  }

  private static boolean isNumeric(String value) {
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) {
        return false;
      }
    }
    return !value.isEmpty();
  }

  private record ParsedVersion(BigInteger major, BigInteger minor, BigInteger patch, String preRelease) {
    private static ParsedVersion parse(String value) {
      var matcher = SEMVER.matcher(value);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Invalid Cargo crate version: " + value);
      }
      return new ParsedVersion(
          new BigInteger(matcher.group(1)),
          new BigInteger(matcher.group(2)),
          new BigInteger(matcher.group(3)),
          matcher.group(4));
    }

    private int compareCore(ParsedVersion other) {
      int majorCompare = major.compareTo(other.major);
      if (majorCompare != 0) {
        return majorCompare;
      }
      int minorCompare = minor.compareTo(other.minor);
      if (minorCompare != 0) {
        return minorCompare;
      }
      return patch.compareTo(other.patch);
    }
  }
}

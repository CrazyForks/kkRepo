package com.github.klboke.kkrepo.server.goartifact;

public enum GoAssetKind {
  PACKAGE,
  INFO,
  LIST,
  LATEST,
  MODULE;

  static GoAssetKind fromExtension(String extension) {
    return switch (extension) {
      case "zip" -> PACKAGE;
      case "info" -> INFO;
      case "mod" -> MODULE;
      default -> throw new IllegalArgumentException("Unsupported Go module extension: " + extension);
    };
  }
}

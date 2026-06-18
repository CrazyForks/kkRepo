package com.github.klboke.nexusplus.protocol.docker;

public class DockerProtocolException extends RuntimeException {
  private final DockerErrorCode code;
  private final int status;

  public DockerProtocolException(DockerErrorCode code, String message) {
    this(code, message, statusFor(code));
  }

  public DockerProtocolException(DockerErrorCode code, String message, int status) {
    super(message == null || message.isBlank() ? code.defaultMessage() : message);
    this.code = code;
    this.status = status;
  }

  public DockerErrorCode code() {
    return code;
  }

  public int status() {
    return status;
  }

  private static int statusFor(DockerErrorCode code) {
    return switch (code) {
      case UNAUTHORIZED -> 401;
      case DENIED -> 403;
      case UNSUPPORTED -> 405;
      default -> 404;
    };
  }
}

package com.github.klboke.kkrepo.server.docker;

import com.github.klboke.kkrepo.protocol.docker.DockerConstants;
import com.github.klboke.kkrepo.protocol.docker.DockerProtocolException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
    DockerRegistryController.class,
    DockerTokenController.class
})
public class DockerErrorAdvice {
  @ExceptionHandler(DockerProtocolException.class)
  public ResponseEntity<Map<String, Object>> dockerError(DockerProtocolException e) {
    return ResponseEntity.status(e.status())
        .header(DockerConstants.API_VERSION_HEADER, DockerConstants.API_VERSION)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("errors", List.of(Map.of(
            "code", e.code().name(),
            "message", e.getMessage()))));
  }
}

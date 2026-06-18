package com.github.klboke.nexusplus.server.docker;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service/rest/v1/docker")
public class DockerTokenController {
  private final DockerAuthService authService;

  public DockerTokenController(DockerAuthService authService) {
    this.authService = authService;
  }

  @GetMapping("/token")
  public ResponseEntity<DockerAuthService.TokenView> token(
      @RequestParam(name = "service", required = false) String service,
      HttpServletRequest request) {
    return ResponseEntity.ok(authService.grant(request, service, scopes(request)));
  }

  private static List<String> scopes(HttpServletRequest request) {
    String[] values = request.getParameterValues("scope");
    return values == null ? List.of() : Arrays.stream(values)
        .filter(value -> value != null && !value.isBlank())
        .toList();
  }
}

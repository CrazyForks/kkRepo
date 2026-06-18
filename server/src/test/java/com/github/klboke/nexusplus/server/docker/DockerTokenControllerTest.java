package com.github.klboke.nexusplus.server.docker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class DockerTokenControllerTest {
  @Test
  void scopeParameterKeepsCommaSeparatedActionsTogether() {
    DockerAuthService authService = mock(DockerAuthService.class);
    DockerAuthService.TokenView token =
        new DockerAuthService.TokenView("token", "token", 900, "2026-06-17T00:00:00Z");
    when(authService.grant(any(), eq("127.0.0.1:18090"),
        eq(List.of("repository:docker-live-hosted/codex/alpine:pull,push"))))
        .thenReturn(token);
    DockerTokenController controller = new DockerTokenController(authService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/service/rest/v1/docker/token");
    request.addParameter("service", "127.0.0.1:18090");
    request.addParameter("scope", "repository:docker-live-hosted/codex/alpine:pull,push");

    var response = controller.token("127.0.0.1:18090", request);

    assertEquals(token, response.getBody());
    verify(authService).grant(request, "127.0.0.1:18090",
        List.of("repository:docker-live-hosted/codex/alpine:pull,push"));
  }
}

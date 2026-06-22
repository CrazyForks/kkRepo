package com.github.klboke.kkrepo.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.klboke.kkrepo.protocol.docker.DockerConstants;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DockerRegistryBlackBoxCompatibilityTest {
  private static final HttpClient HTTP = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(20))
      .followRedirects(HttpClient.Redirect.NEVER)
      .build();

  @Test
  void registryV2EndpointsMatchNexusWhenConfigured() throws Exception {
    CompatConfig config = CompatConfig.load();
    assumeTrue(config.enabled(),
        "Set COMPAT_DOCKER_ENABLED=true to run Docker Registry V2 black-box compatibility");
    assumeTrue(config.configured(),
        "Set NEXUS_COMPAT_BASE_URL and NEXUS_PLUS_COMPAT_BASE_URL to run Docker compatibility");

    Exchange referenceBase = get(config.nexus().v2Base());
    Exchange candidateBase = get(config.nexusPlus().v2Base());
    assertStatusFamily("GET /v2/", referenceBase, candidateBase);
    assertHeaderEquals("base API version", referenceBase, candidateBase, DockerConstants.API_VERSION_HEADER);

    Exchange referenceHeadBase = head(config.nexus().v2Base());
    Exchange candidateHeadBase = head(config.nexusPlus().v2Base());
    assertStatusFamily("HEAD /v2/", referenceHeadBase, candidateHeadBase);
    assertHeaderEquals("base HEAD API version", referenceHeadBase, candidateHeadBase,
        DockerConstants.API_VERSION_HEADER);

    if (!config.image().isBlank() && !config.reference().isBlank()) {
      String manifestPath = config.repositoryPath(config.image() + "/manifests/" + config.reference());
      Exchange referenceManifestHead = head(config.nexus().v2(manifestPath));
      Exchange candidateManifestHead = head(config.nexusPlus().v2(manifestPath));
      assertStatusFamily("HEAD manifest", referenceManifestHead, candidateManifestHead);
      if (referenceManifestHead.status() == 200 && candidateManifestHead.status() == 200) {
        assertHeaderPresent("manifest digest", referenceManifestHead, DockerConstants.CONTENT_DIGEST_HEADER);
        assertHeaderPresent("manifest digest", candidateManifestHead, DockerConstants.CONTENT_DIGEST_HEADER);
        assertHeaderPresent("manifest content type", referenceManifestHead, "Content-Type");
        assertHeaderPresent("manifest content type", candidateManifestHead, "Content-Type");
      }

      Exchange referenceManifest = get(config.nexus().v2(manifestPath), dockerAccept());
      Exchange candidateManifest = get(config.nexusPlus().v2(manifestPath), dockerAccept());
      assertStatusFamily("GET manifest", referenceManifest, candidateManifest);
      if (referenceManifest.status() == 200 && candidateManifest.status() == 200) {
        assertHeaderPresent("manifest digest", referenceManifest, DockerConstants.CONTENT_DIGEST_HEADER);
        assertHeaderPresent("manifest digest", candidateManifest, DockerConstants.CONTENT_DIGEST_HEADER);
      }

      Exchange referenceTags = get(config.nexus().v2(config.repositoryPath(config.image() + "/tags/list?n=1")));
      Exchange candidateTags = get(config.nexusPlus().v2(config.repositoryPath(config.image() + "/tags/list?n=1")));
      assertStatusFamily("GET tags/list", referenceTags, candidateTags);
      if (referenceTags.status() == 200 && candidateTags.status() == 200) {
        assertTrue(new String(referenceTags.body()).contains("\"tags\""), "reference tags body should be registry JSON");
        assertTrue(new String(candidateTags.body()).contains("\"tags\""), "kkrepo tags body should be registry JSON");
      }
    }

    String uploadStartPath = config.repositoryPath(config.uploadImage() + "/blobs/uploads/");
    Exchange referenceUploadStart = post(config.nexus().v2(uploadStartPath));
    Exchange candidateUploadStart = post(config.nexusPlus().v2(uploadStartPath));
    assertStatusFamily("POST blobs/uploads", referenceUploadStart, candidateUploadStart);
    if (isUploadAccepted(referenceUploadStart.status()) && isUploadAccepted(candidateUploadStart.status())) {
      assertHeaderPresent("upload location", referenceUploadStart, "Location");
      assertHeaderPresent("upload location", candidateUploadStart, "Location");
      assertHeaderPresent("upload uuid", candidateUploadStart, DockerConstants.UPLOAD_UUID_HEADER);
    }
  }

  private static Exchange get(URI uri) throws Exception {
    return get(uri, "*/*");
  }

  private static Exchange get(URI uri, String accept) throws Exception {
    return send(HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(60))
        .header("Accept", accept)
        .header("User-Agent", "kkrepo-docker-compat-test/1")
        .GET());
  }

  private static Exchange head(URI uri) throws Exception {
    return send(HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(60))
        .header("Accept", dockerAccept())
        .header("User-Agent", "kkrepo-docker-compat-test/1")
        .method("HEAD", HttpRequest.BodyPublishers.noBody()));
  }

  private static Exchange post(URI uri) throws Exception {
    return send(HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(60))
        .header("Content-Length", "0")
        .header("User-Agent", "kkrepo-docker-compat-test/1")
        .POST(HttpRequest.BodyPublishers.noBody()));
  }

  private static Exchange send(HttpRequest.Builder builder) throws Exception {
    HttpResponse<byte[]> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    Map<String, String> headers = new LinkedHashMap<>();
    response.headers().map().forEach((key, values) -> {
      if (!values.isEmpty()) {
        headers.put(key.toLowerCase(Locale.ROOT), values.get(0));
      }
    });
    return new Exchange(response.statusCode(), headers, response.body());
  }

  private static void assertStatusFamily(String label, Exchange reference, Exchange candidate) {
    if (reference.status() == 401 || candidate.status() == 401) {
      assertEquals(reference.status(), candidate.status(), label + " auth challenge status");
      assertHeaderPresent(label + " auth challenge", reference, "WWW-Authenticate");
      assertHeaderPresent(label + " auth challenge", candidate, "WWW-Authenticate");
      return;
    }
    assertEquals(reference.status() / 100, candidate.status() / 100,
        label + " status family: reference=" + reference.status() + " candidate=" + candidate.status());
  }

  private static void assertHeaderEquals(String label, Exchange reference, Exchange candidate, String name) {
    String referenceValue = reference.header(name).orElse("");
    String candidateValue = candidate.header(name).orElse("");
    assertEquals(referenceValue, candidateValue, label);
  }

  private static void assertHeaderPresent(String label, Exchange exchange, String name) {
    assertFalse(exchange.header(name).orElse("").isBlank(), label + " missing " + name);
  }

  private static boolean isUploadAccepted(int status) {
    return status == 202 || status == 201;
  }

  private static String dockerAccept() {
    return String.join(", ",
        DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST,
        DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST_LIST,
        DockerConstants.MEDIA_TYPE_OCI_MANIFEST,
        DockerConstants.MEDIA_TYPE_OCI_INDEX,
        DockerConstants.MEDIA_TYPE_OCI_ARTIFACT);
  }

  private static Optional<String> setting(String property, String env) {
    return CompatDefaults.setting(property, env);
  }

  private static String stripTrailingSlash(String value) {
    return CompatDefaults.stripTrailingSlash(value);
  }

  private record CompatConfig(
      boolean enabled,
      Endpoint nexus,
      Endpoint nexusPlus,
      String repository,
      String image,
      String reference,
      String uploadImage,
      boolean pathBased) {
    static CompatConfig load() {
      boolean enabled = Boolean.parseBoolean(
          setting("compat.docker.enabled", "COMPAT_DOCKER_ENABLED").orElse("false"));
      boolean pathBased = Boolean.parseBoolean(
          setting("compat.docker.pathBased", "COMPAT_DOCKER_PATH_BASED").orElse("true"));
      String repository = setting("compat.docker.repository", "COMPAT_DOCKER_REPOSITORY")
          .orElse("docker-hosted");
      return new CompatConfig(
          enabled,
          new Endpoint("nexus",
              setting("compat.docker.nexus.baseUrl", "DOCKER_NEXUS_COMPAT_BASE_URL")
                  .or(() -> CompatDefaults.nexusBaseUrl()),
              CompatDefaults.nexusUsername(),
              CompatDefaults.nexusPassword()),
          new Endpoint("kkrepo",
              setting("compat.docker.nexusPlus.baseUrl", "DOCKER_NEXUS_PLUS_COMPAT_BASE_URL")
                  .or(() -> CompatDefaults.nexusPlusBaseUrl()),
              CompatDefaults.nexusPlusUsername(),
              CompatDefaults.nexusPlusPassword()),
          repository,
          setting("compat.docker.image", "COMPAT_DOCKER_IMAGE").orElse("library/alpine"),
          setting("compat.docker.reference", "COMPAT_DOCKER_REFERENCE").orElse("latest"),
          setting("compat.docker.uploadImage", "COMPAT_DOCKER_UPLOAD_IMAGE")
              .orElse("kkrepo-compat/upload-probe"),
          pathBased);
    }

    boolean configured() {
      return nexus.baseUrl().isPresent() && nexusPlus.baseUrl().isPresent();
    }

    String repositoryPath(String path) {
      if (!pathBased) {
        return path;
      }
      return repository + "/" + path;
    }
  }

  private record Endpoint(
      String name,
      Optional<String> baseUrl,
      Optional<String> username,
      Optional<String> password) {
    URI v2Base() {
      return v2("");
    }

    URI v2(String path) {
      String normalized = path == null ? "" : path;
      while (normalized.startsWith("/")) {
        normalized = normalized.substring(1);
      }
      String suffix = normalized.isBlank() ? "" : "/" + normalized;
      return URI.create(baseUrl.orElseThrow() + "/v2" + suffix);
    }
  }

  private record Exchange(int status, Map<String, String> headers, byte[] body) {
    Optional<String> header(String name) {
      return Optional.ofNullable(headers.get(name.toLowerCase(Locale.ROOT)));
    }
  }
}

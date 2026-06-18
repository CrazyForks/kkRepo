package com.github.klboke.nexusplus.protocol.docker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DockerManifestAcceptTest {
  @Test
  void absentOrWildcardAcceptAllowsStoredManifestType() {
    assertTrue(DockerManifestAccept.accepts(List.of(), DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST));
    assertTrue(DockerManifestAccept.accepts(List.of("*/*"), DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST));
    assertTrue(DockerManifestAccept.accepts(List.of("application/*"), DockerConstants.MEDIA_TYPE_OCI_INDEX));
  }

  @Test
  void commaSeparatedManifestAcceptMatchesIgnoringParameters() {
    assertTrue(DockerManifestAccept.accepts(
        List.of(DockerConstants.MEDIA_TYPE_OCI_INDEX + "; q=0.8, "
            + DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST),
        DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST));
  }

  @Test
  void mismatchedManifestAcceptIsRejected() {
    assertFalse(DockerManifestAccept.accepts(
        List.of(DockerConstants.MEDIA_TYPE_OCI_INDEX),
        DockerConstants.MEDIA_TYPE_SCHEMA2_MANIFEST));
  }
}

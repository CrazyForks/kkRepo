package com.github.klboke.kkrepo.protocol.cargo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CargoVersionsTest {

  @Test
  void requireVersionRejectsNonSemverValues() {
    assertEquals("1.2.3", CargoVersions.requireVersion(" 1.2.3 "));
    assertEquals("1.2.3-alpha.1+build.5", CargoVersions.requireVersion("1.2.3-alpha.1+build.5"));

    assertThrows(IllegalArgumentException.class, () -> CargoVersions.requireVersion("latest"));
    assertThrows(IllegalArgumentException.class, () -> CargoVersions.requireVersion("1"));
    assertThrows(IllegalArgumentException.class, () -> CargoVersions.requireVersion("1.0"));
    assertThrows(IllegalArgumentException.class, () -> CargoVersions.requireVersion("01.0.0"));
    assertThrows(IllegalArgumentException.class, () -> CargoVersions.requireVersion("1.0.0/evil"));
  }

  @Test
  void compareUsesSemverPreReleaseOrderingAndIgnoresBuildMetadata() {
    assertTrue(CargoVersions.compare("1.0.0", "1.0.0-rc.1") > 0);
    assertTrue(CargoVersions.compare("1.0.0-alpha.2", "1.0.0-alpha.1") > 0);
    assertTrue(CargoVersions.compare("1.10.0", "1.2.0") > 0);
    assertEquals(0, CargoVersions.compare("1.0.0+build.2", "1.0.0+build.1"));
  }
}

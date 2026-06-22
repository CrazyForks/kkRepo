package com.github.klboke.kkrepo.core;

public record ProtocolCapability(
    boolean hostedRead,
    boolean hostedWrite,
    boolean proxyRead,
    boolean groupRead,
    boolean nexusPathCompatible) {
}

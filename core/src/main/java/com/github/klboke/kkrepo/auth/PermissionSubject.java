package com.github.klboke.kkrepo.auth;

import java.io.Serializable;
import java.util.Set;

public record PermissionSubject(
    String source,
    String userId,
    Set<String> groupIds,
    String tokenId) implements Serializable {
}

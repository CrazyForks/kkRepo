package com.github.klboke.kkrepo.auth;

import com.github.klboke.kkrepo.core.RepositoryFormat;

public record RepositoryPermission(
    String repository,
    RepositoryFormat format,
    String pathPattern,
    PermissionAction action) {
}

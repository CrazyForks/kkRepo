package com.github.klboke.kkrepo.auth;

public interface AccessDecisionService {
  AccessDecision decide(PermissionSubject subject, RepositoryPermission permission);
}

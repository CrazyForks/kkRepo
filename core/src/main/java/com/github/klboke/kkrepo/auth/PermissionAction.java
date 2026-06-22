package com.github.klboke.kkrepo.auth;

public enum PermissionAction {
  BROWSE("browse"),
  READ("read"),
  ADD("add"),
  EDIT("edit"),
  WRITE("edit"),
  DELETE("delete"),
  ADMIN("*");

  private final String nexusAction;

  PermissionAction(String nexusAction) {
    this.nexusAction = nexusAction;
  }

  public String nexusAction() {
    return nexusAction;
  }
}

package com.github.klboke.kkrepo.server.pypi;

public record PypiLink(String file, String href, String requiresPython) {
  public PypiLink {
    if (file == null) file = "";
    if (href == null) href = "";
    if (requiresPython == null) requiresPython = "";
  }
}

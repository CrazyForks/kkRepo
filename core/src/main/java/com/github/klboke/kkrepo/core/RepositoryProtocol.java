package com.github.klboke.kkrepo.core;

public interface RepositoryProtocol {
  RepositoryFormat format();

  ProtocolCapability capability();
}

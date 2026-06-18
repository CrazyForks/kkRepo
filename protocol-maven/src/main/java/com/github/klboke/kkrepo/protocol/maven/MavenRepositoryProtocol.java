package com.github.klboke.kkrepo.protocol.maven;

import com.github.klboke.kkrepo.core.ProtocolCapability;
import com.github.klboke.kkrepo.core.RepositoryFormat;
import com.github.klboke.kkrepo.core.RepositoryProtocol;

public final class MavenRepositoryProtocol implements RepositoryProtocol {
  @Override
  public RepositoryFormat format() {
    return RepositoryFormat.MAVEN2;
  }

  @Override
  public ProtocolCapability capability() {
    return new ProtocolCapability(true, true, false, false, true);
  }
}

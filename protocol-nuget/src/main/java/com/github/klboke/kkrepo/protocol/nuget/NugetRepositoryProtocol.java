package com.github.klboke.kkrepo.protocol.nuget;

import com.github.klboke.kkrepo.core.ProtocolCapability;
import com.github.klboke.kkrepo.core.RepositoryFormat;
import com.github.klboke.kkrepo.core.RepositoryProtocol;

public final class NugetRepositoryProtocol implements RepositoryProtocol {
  @Override
  public RepositoryFormat format() {
    return RepositoryFormat.NUGET;
  }

  @Override
  public ProtocolCapability capability() {
    return new ProtocolCapability(true, true, true, true, true);
  }
}

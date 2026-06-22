package com.github.klboke.kkrepo.protocol.npm;

import com.github.klboke.kkrepo.core.ProtocolCapability;
import com.github.klboke.kkrepo.core.RepositoryFormat;
import com.github.klboke.kkrepo.core.RepositoryProtocol;

public final class NpmRepositoryProtocol implements RepositoryProtocol {
  @Override
  public RepositoryFormat format() {
    return RepositoryFormat.NPM;
  }

  @Override
  public ProtocolCapability capability() {
    return new ProtocolCapability(true, true, false, false, true);
  }
}

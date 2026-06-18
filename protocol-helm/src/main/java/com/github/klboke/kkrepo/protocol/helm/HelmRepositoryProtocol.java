package com.github.klboke.kkrepo.protocol.helm;

import com.github.klboke.kkrepo.core.ProtocolCapability;
import com.github.klboke.kkrepo.core.RepositoryFormat;
import com.github.klboke.kkrepo.core.RepositoryProtocol;

public final class HelmRepositoryProtocol implements RepositoryProtocol {
  @Override
  public RepositoryFormat format() {
    return RepositoryFormat.HELM;
  }

  @Override
  public ProtocolCapability capability() {
    return new ProtocolCapability(true, true, true, false, true);
  }
}

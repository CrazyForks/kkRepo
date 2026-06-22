package com.github.klboke.kkrepo.server.catalog;

public interface CatalogCacheBroadcaster {
  void subscribe(String catalogName, Runnable refreshListener);

  void publishRefresh(String catalogName);
}

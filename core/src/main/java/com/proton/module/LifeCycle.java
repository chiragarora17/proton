package com.proton.module;

import javax.inject.Singleton;

import com.netflix.governator.spi.LifecycleListener;

@Singleton
public interface LifeCycle extends LifecycleListener {
  default void onStarted() {
    startup();
  }

  default void onStopped(Throwable t){
    shutdown();
  }

  void startup();

  void shutdown();
}

package com.proton.module;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class DumbClass implements LifeCycle {
  AtomicBoolean started = new AtomicBoolean(false);
  AtomicBoolean stopped = new AtomicBoolean(false);

  @Inject()
  public DumbClass(){
  }

  public String getHello() {
    return "Hello";
  }

  @Override
  public void startup() {
    started.getAndSet(true);
  }

  @Override
  public void shutdown() {
    stopped.getAndSet(true);
  }
}
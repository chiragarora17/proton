package com.proton.module;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.netflix.governator.LifecycleInjector;

public class LifeCyleTest {
  @Test
  public void testLifeCycle() throws Exception {
    LifecycleInjector injector = new InjectorBuilder().addModules(new AbstractModule() {
      @Override
      protected void configure() {
        bind(DumbClass.class).asEagerSingleton();
      }
    }).buildAsLifecycleInjector();

    DumbClass dumbClass = injector.getInstance(DumbClass.class);

    Assert.assertEquals("Hello", dumbClass.getHello());
    Assert.assertTrue(dumbClass.started.get());
    Assert.assertFalse(dumbClass.stopped.get());

    injector.close();

    Assert.assertTrue(dumbClass.stopped.get());
  }
}

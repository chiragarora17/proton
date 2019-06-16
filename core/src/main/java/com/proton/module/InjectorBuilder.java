package com.proton.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.ShutdownHookModule;
import com.netflix.governator.spi.LifecycleListener;

public class InjectorBuilder {
  private final List<Module> modules = new ArrayList<>();
  private final List<LifecycleListener> listeners = new ArrayList<>();
  private final List<ServiceDescriptor> descriptors = new ArrayList<>();

  public InjectorBuilder addModules(Module... module) {
    addModules(Arrays.asList(module));
    return this;
  }

  public InjectorBuilder addModules(Collection<? extends Module> module) {
    modules.addAll(module);
    return this;
  }

  public InjectorBuilder addLifecycleListener(LifecycleListener... listener) {
    addLifecycleListener(Arrays.asList(listener));
    return this;
  }

  public InjectorBuilder addLifecycleListener(Collection<LifecycleListener> listener) {
    listeners.addAll(listener);
    return this;
  }

  public InjectorBuilder addServiceDescriptors(ServiceDescriptor... descriptor) {
    this.descriptors.addAll(Arrays.asList(descriptor));
    return this;
  }

  public InjectorBuilder addServiceDescriptors(Collection<? extends ServiceDescriptor> descriptors) {
    this.descriptors.addAll(descriptors);
    return this;
  }

  public Injector build() {
    return buildAsLifecycleInjector(Stage.PRODUCTION);
  }

  public Injector build(Stage stage) {
    return buildAsLifecycleInjector(stage);
  }

  public LifecycleInjector buildAsLifecycleInjector() {
    return buildAsLifecycleInjector(Stage.PRODUCTION);
  }


  public LifecycleInjector buildAsLifecycleInjector (Stage stage) {
    modules.add(new ShutdownHookModule());
    descriptors.forEach(sd -> modules.add(sd.getService()));
    LifecycleInjector injector = com.netflix.governator.InjectorBuilder
      .fromModules(modules)
      .createInjector(stage);

    listeners.forEach(injector::addListener);

    injector.addListener(new LifecycleListener() {
      @Override
      public void onStarted() {}

      @Override
      public void onStopped(Throwable throwable) {
        descriptors.forEach(ServiceDescriptor::shutdown);
      }
    });

    return injector;
  }
}

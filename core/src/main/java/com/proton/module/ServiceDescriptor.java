package com.proton.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.netflix.governator.LifecycleInjector;
import com.netflix.governator.spi.LifecycleListener;
import com.proton.config.ServiceConfig;
import com.proton.module.ServiceProvider;

public class ServiceDescriptor {
  private static final Logger logger = LogManager.getLogger(ServiceDescriptor.class);

  private final ServiceName name;
  private final LifecycleInjector injector;
  private final ServiceProvider serviceProvider;

  private ServiceDescriptor(ServiceName name, LifecycleInjector injector, ServiceProvider serviceProvider) {
    this.name = name;
    this.injector = injector;
    this.serviceProvider = serviceProvider;
  }

  public static ServiceDescriptor of(String name, Class<? extends ServiceProvider<?>> serviceProvider) {
    return new Builder(ServiceName.of(name), serviceProvider).build();
  }

  public ServiceName serviceName() {
    return name;
  }

  public ServiceProvider getService() {
    return serviceProvider;
  }

  public void shutdown() {
    injector.close();
  }

  public static class Builder {
    private final List<Module> moduleList = new ArrayList<>();
    private final List<LifecycleListener> lifecycleListeners = new ArrayList<>();
    private final List<ServiceDescriptor> serviceDescriptors = new ArrayList<>();
    private final ServiceName serviceName;
    private final Class<? extends ServiceProvider> serviceProviderClass;

    public Builder(ServiceName serviceName, Class<? extends ServiceProvider<?>> serviceProviderClass) {
      this.serviceName = serviceName;
      this.serviceProviderClass = serviceProviderClass;
    }

    public Builder addModules(Module... modules) {
      moduleList.addAll(Arrays.asList(modules));
      return this;
    }

    public Builder addModules(List<? extends Module> moduleList) {
      this.moduleList.addAll(moduleList);
      return this;
    }

    public Builder addLifeCycleListeners(LifecycleListener... listeners) {
      lifecycleListeners.addAll(Arrays.asList(listeners));
      return this;
    }

    public Builder addLifeCycleListeners(List<? extends LifecycleListener> lifecycleListeners) {
      this.lifecycleListeners.addAll(lifecycleListeners);
      return this;
    }

    public Builder addServiceDependencies(ServiceDescriptor... serviceDescriptors) {
      this.serviceDescriptors.addAll(Arrays.asList(serviceDescriptors));
      return this;
    }

    public Builder addServiceDependencies(List<? extends ServiceDescriptor> serviceDescriptors) {
      this.serviceDescriptors.addAll(serviceDescriptors);
      return this;
    }

    public ServiceDescriptor build() {
      logger.info("Creating and Starting service {} ...", serviceName);
      LifecycleInjector injector = new InjectorBuilder()
        .addModules(moduleList)
        .addModules(new ServiceConfig(serviceName.getPath()))
        .addModules(new AbstractModule() {
          @Override
          protected void configure() {
            bind(ServiceName.class).toInstance(serviceName);
          }
        })
        .addLifecycleListener(lifecycleListeners)
        .addServiceDescriptors(serviceDescriptors)
        .buildAsLifecycleInjector();

      ServiceProvider provider = injector.getInstance(serviceProviderClass);

      return new ServiceDescriptor(serviceName, injector, provider);
    }
  }
}

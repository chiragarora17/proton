package com.proton.module

import javax.inject.Provider

import com.google.common.reflect.TypeToken

trait ServiceType[T] {
  protected val serviceClass: Class[_>: T] = new TypeToken[T](getClass) {}.getRawType
}

abstract class ServiceProvider[T] extends NebulaModule with ServiceType[T] with Provider[T] {
  override def configure(): Unit = {
    bind(serviceClass).toProvider(this)
    expose(serviceClass)
  }
}

package com.proton.module;

import com.google.inject.PrivateModule;

public class NebulaModule extends PrivateModule {
  /**
   * don't use configure() everything should be @Provides and @Exposed if you need to make it public
   */
  @Override
  protected void configure() {
  }
}

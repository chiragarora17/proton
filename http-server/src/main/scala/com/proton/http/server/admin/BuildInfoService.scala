package com.proton.http.server.admin

import java.util.Properties
import javax.inject.Inject

import com.proton.http.server.Response

object BuildInfoService {
  val buildInfo = new Properties()
  Option(getClass.getClassLoader.getResourceAsStream("build.info")).foreach(buildInfo.load)
}

class BuildInfoService @Inject()() extends AdminService() {
  get("admin", "buildInfo") { request =>
    Response.ok(BuildInfoService.buildInfo)
  }
}

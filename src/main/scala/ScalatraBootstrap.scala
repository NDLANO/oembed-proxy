/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

import javax.servlet.ServletContext

import no.ndla.oembedproxy.controller.OEmbedProxyController
import no.ndla.oembedproxy.{OEmbedSwagger, ResourcesApp}
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle{
  implicit val swagger = new OEmbedSwagger

  override def init(context: ServletContext) {
    context.mount(new OEmbedProxyController, "/oembed")
    context.mount(new ResourcesApp, "/api-docs")
  }
}

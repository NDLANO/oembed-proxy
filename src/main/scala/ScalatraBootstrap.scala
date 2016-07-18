/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

import javax.servlet.ServletContext

import no.ndla.oembedproxy.ComponentRegistry
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle{

  override def init(context: ServletContext) {
    context.mount(ComponentRegistry.oEmbedProxyController, "/oembed")
    context.mount(ComponentRegistry.resourcesApp, "/api-docs")
  }
}

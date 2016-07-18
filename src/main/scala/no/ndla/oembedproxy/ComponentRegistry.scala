/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import no.ndla.network.NdlaClient
import no.ndla.oembedproxy.controller.OEmbedProxyController
import no.ndla.oembedproxy.service.{OEmbedServiceComponent, ProviderService}


object ComponentRegistry extends OEmbedProxyController with OEmbedServiceComponent with NdlaClient with ProviderService{
  implicit val swagger = new OEmbedSwagger

  lazy val providerService = new ProviderService
  lazy val oEmbedService = new OEmbedService(providerService.loadProviders())
  lazy val ndlaClient = new NdlaClient
  lazy val oEmbedProxyController = new OEmbedProxyController
  lazy val resourcesApp = new ResourcesApp
}

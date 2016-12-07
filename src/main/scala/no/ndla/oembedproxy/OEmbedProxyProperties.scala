/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy


object OEmbedProxyProperties {
  val ApplicationPort = 80

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"

  val ContactEmail = "christergundersen@ndla.no"
  val JSonProviderUrl = "http://oembed.com/providers.json"
  val HttpNdlaOembedServiceUrl = "http://ndla.no/services/oembed"
  val HttpsNdlaOembedServiceUrl = "https://ndla.no/services/oembed"

  val OembedProxyControllerMountPoint = "/oembed"
  val ResourcesAppMountPoint = "/api-docs"
  val HealthControllerMountPoint = "/health"
}

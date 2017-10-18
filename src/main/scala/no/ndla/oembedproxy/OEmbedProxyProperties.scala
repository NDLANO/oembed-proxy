/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import no.ndla.network.Domains
import scala.util.Properties.envOrNone


object OEmbedProxyProperties {
  val ApplicationPort = propOrElse("APPLICATION_PORT", "80").toInt

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"
  val Environment = propOrElse("NDLA_ENVIRONMENT", "local")

  val ContactEmail = "christergundersen@ndla.no"
  val JSonProviderUrl = "https://oembed.com/providers.json"
  val ProviderListCacheAgeInMs: Long = 1000 * 60 * 60 * 24 // 24 hour caching

  val NdlaApiOembedServiceUrl = Map(
    "local" -> "http://ndla-frontend.ndla-local:3000/oembed",
    "prod" -> "https://ndla-frontend.api.ndla.no/oembed"
  ).getOrElse(Environment, s"https://ndla-frontend.$Environment.api.ndla.no/oembed")

  val NdlaApiOembedProvider = Domain
  val NdlaApprovedUrl = Map(
    "local" -> "http://proxy.ndla-local:30017/article/*",
    "prod" -> "https?://ndla-frontend.api.ndla.no/article/*"
  ).getOrElse(Environment, s"https?://ndla-frontend.$Environment.api.ndla.no/article/*")

  val OembedProxyControllerMountPoint = "/oembed-proxy/v1/oembed"
  val ResourcesAppMountPoint = "/oembed-proxy/api-docs"
  val HealthControllerMountPoint = "/health"


  lazy val Domain = Domains.get(Environment)

  def propOrElse(key: String, default: => String): String = {
    envOrNone(key) match {
      case Some(env) => env
      case None => default
    }
  }
}

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
  val Auth0LoginEndpoint = "https://ndla.eu.auth0.com/authorize"

  val ApplicationPort = propOrElse("APPLICATION_PORT", "80").toInt

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"
  val Environment = propOrElse("NDLA_ENVIRONMENT", "local")

  val ContactEmail = "christergundersen@ndla.no"
  val JSonProviderUrl = "https://oembed.com/providers.json"
  val ProviderListCacheAgeInMs: Long = 1000 * 60 * 60 * 24 // 24 hour caching
  val ProviderListRetryTimeInMs: Long = 1000 * 60 * 60 // 1 hour before retrying a failed attempt.

  val NdlaApiOembedServiceUrl = Map(
    "local" -> "http://ndla-frontend.ndla-local:3000/oembed",
    "prod" -> "https://ndla-frontend.api.ndla.no/oembed"
  ).getOrElse(Environment, s"https://ndla-frontend.$Environment.api.ndla.no/oembed")

  val NdlaApiOembedProvider = Domain
  val NdlaApprovedUrl = Map(
    "local" -> "http://api-gateway.ndla-local:30017/*",
    "prod" -> "https?://beta.ndla.no/*"
  ).getOrElse(Environment, s"https?://ndla-frontend.$Environment.api.ndla.no/*")

  val NdlaH5POembedProvider = Map(
    "brukertest" -> "https://h5p.ndla.no",
    "spoletest" -> "https://h5p.ndla.no",
    "prod" -> "https://h5p.ndla.no"
  ).getOrElse(Environment, "https://h5p-test.ndla.no")
  val NdlaH5PApprovedUrl = Map(
    "brukertest" -> "https://h5p.ndla.no/resource/*",
    "spoletest" -> "https://h5p.ndla.no/resource/*",
    "prod" -> "https://h5p.ndla.no/resource/*"
  ).getOrElse(Environment, "https://h5p-test.ndla.no/resource/*")

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

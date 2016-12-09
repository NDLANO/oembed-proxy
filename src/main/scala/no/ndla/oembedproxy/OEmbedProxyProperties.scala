/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import scala.util.Properties.envOrNone


object OEmbedProxyProperties {
  val ApplicationPort = 80

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"
  val Environment = propOrElse("NDLA_ENVIRONMENT", "local")

  val ContactEmail = "christergundersen@ndla.no"
  val JSonProviderUrl = "http://oembed.com/providers.json"

  val NdlaApiOembedServiceUrl = s"$Domain/article-oembed"
  val NdlaApiOembedProvider = Domain
  val NdlaApprovedUrl = s"$Domain:8082/article/*"

  val OembedProxyControllerMountPoint = "/oembed"
  val ResourcesAppMountPoint = "/api-docs"
  val HealthControllerMountPoint = "/health"


  lazy val Domain = Map(
    "local" -> "http://localhost",
    "prod" -> "http://api.ndla.no"
  ).getOrElse(Environment, s"http://api.$Environment.ndla.no")

  def propOrElse(key: String, default: => String): String = {
        envOrNone(key) match {
          case Some(env) => env
          case None => default
        }
  }
}
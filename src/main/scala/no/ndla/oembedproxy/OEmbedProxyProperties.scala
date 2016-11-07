/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.io.Source


object OEmbedProxyProperties extends LazyLogging {
  var OEmbedApiProps: mutable.Map[String, Option[String]] = mutable.HashMap()
  val ApplicationPort = 80

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"

  lazy val ContactEmail = "christergundersen@ndla.no"
  lazy val JSonProviderUrl = "http://oembed.com/providers.json"
  lazy val NdlaOembedServiceUrl = "http://ndla.no/services/oembed"

  val OembedProxyControllerMountPoint = "/oembed"
  val ResourcesAppMountPoint = "/api-docs"
  val HealthControllerMountPoint = "/health"
}

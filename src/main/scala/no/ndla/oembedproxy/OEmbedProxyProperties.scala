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

  lazy val ContactEmail = get("CONTACT_EMAIL")
  lazy val HostAddr = get("HOST_ADDR")
  lazy val JSonProviderUrl = get("JSON_PROVIDERS_URL")
  lazy val NdlaOembedServiceUrl = get("NDLA_OEMBED_SERVICE_URL")

  val OembedProxyControllerMountPoint = "/oembed"
  val ResourcesAppMountPoint = "/api-docs"
  val HealthControllerMountPoint = "/health"

  def setProperties(properties: Map[String, Option[String]]) = {
    properties.foreach(prop => OEmbedApiProps.put(prop._1, prop._2))
  }

  def verify() = {
    val missingProperties = OEmbedApiProps.filter(entry => entry._2.isEmpty).toList
    if (missingProperties.nonEmpty) {
      missingProperties.foreach(entry => logger.error("Missing required environment variable {}", entry._1))

      logger.error("Shutting down.")
      System.exit(1)
    }
  }

  private def get(envKey: String): String = {
    OEmbedApiProps.get(envKey).flatten match {
      case Some(value) => value
      case None => throw new NoSuchFieldError(s"Missing environment variable $envKey")
    }
  }
}

object PropertiesLoader {
  val EnvironmentFile = "/oembed-proxy.env"

  private def readPropertyFile(): Map[String, Option[String]] = {
    val keys = Source.fromInputStream(getClass.getResourceAsStream(EnvironmentFile)).getLines().withFilter(line => line.matches("^\\w+$"))
    keys.map(key => key -> scala.util.Properties.envOrNone(key)).toMap
  }

  def load() = {
    OEmbedProxyProperties.setProperties(readPropertyFile())
    OEmbedProxyProperties.verify()
  }
}

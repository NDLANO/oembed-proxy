/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.NdlaClient
import no.ndla.oembedproxy.ComponentRegistry
import no.ndla.oembedproxy.model.{OEmbed, OEmbedProvider, ProviderNotSupportedException}

import scala.util.Try
import scalaj.http.{Http, HttpOptions}


trait OEmbedServiceComponent extends LazyLogging {
  this: NdlaClient =>
  val oEmbedService: OEmbedService

  class OEmbedService(optionalProviders: Option[List[OEmbedProvider]] = None) {
    implicit val formats = org.json4s.DefaultFormats

    def get(url: String, maxWidth: Option[String], maxHeight: Option[String]): Try[OEmbed] = {
      val p = optionalProviders.toList.flatten ++ ComponentRegistry.providerService.loadProviders()
      p.find(_.supports(url)) match {
        case None => throw new ProviderNotSupportedException(s"Could not find an oembed-provider for the url '$url'")
        case Some(provider) =>
          ndlaClient.fetch[OEmbed](
            Http(provider.requestUrl(url, maxWidth, maxHeight)).option(HttpOptions.followRedirects(true))
          )
      }
    }
  }

}

/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import com.typesafe.scalalogging.LazyLogging
import no.ndla.oembedproxy.model.{HttpRequestException, ProviderNotSupportedException, OEmbedProvider, OEmbed}
import org.json4s.native.JsonMethods._

import scala.util.Try
import scalaj.http.{HttpOptions, Http, HttpRequest}


trait OEmbedServiceComponent extends LazyLogging {
  val oEmbedService: OEmbedService

  class OEmbedService(providers: List[OEmbedProvider]) {
    implicit val formats = org.json4s.DefaultFormats

    def get(url: String, maxWidth: Option[String], maxHeight: Option[String]): OEmbed = {
      providers.find(_.supports(url)) match {
        case None => throw new ProviderNotSupportedException(s"Could not find an oembed-provider for the url '$url'")
        case Some(provider) => {
          get(provider, url, maxWidth, maxHeight)
        }
      }
    }

    def get(provider: OEmbedProvider, url: String, maxWidth: Option[String], maxHeight: Option[String]) : OEmbed = {
      val request = Http(provider.requestUrl(url, maxWidth, maxHeight)).option(HttpOptions.followRedirects(true))
      get(request)
    }

    def get(request: HttpRequest): OEmbed = {
      val response = request.asString
      response.isError match {
        case true => throw new HttpRequestException(s"Got ${response.code} ${response.statusLine} when calling ${request.url}")
        case false => {
          val json = response.body
          try {
            parse(json).camelizeKeys.extract[OEmbed]
          } catch {
            case e: Exception => {
              logger.warn(s"Could not parse response: $json", e)
              throw new HttpRequestException(s"Unreadable response from ${request.url}")
            }
          }
        }
      }
    }
  }
}

/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import com.netaporter.uri.dsl._
import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.NdlaClient
import no.ndla.oembedproxy.OEmbedProxyProperties
import no.ndla.oembedproxy.model.{OEmbedEndpoint, OEmbedProvider}

import scala.util.{Failure, Success}
import scalaj.http.{Http, HttpRequest}

trait ProviderService {
  this: NdlaClient =>
  val providerService: ProviderService

  class ProviderService extends LazyLogging {
    implicit val formats = org.json4s.DefaultFormats

    val goOpenEndpoint = OEmbedEndpoint(None, Some("http://www.goopen.no/"), None, None, List("oembed=true"))
    val goOpenProvider = OEmbedProvider("GoOpen.no", "http://www.goopen.no", goOpenEndpoint :: Nil)

    val httpNdlaApprovedUrls = List("http://ndla.no/*/node/*", "http://ndla.no/node/*")
    val httpNdlaEndpoint = OEmbedEndpoint(Some(httpNdlaApprovedUrls), Some(OEmbedProxyProperties.HttpNdlaOembedServiceUrl), None, None)
    val httpNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(httpNdlaEndpoint), url => url.removeAllParams())

    val httpsNdlaApprovedUrls = List("https://ndla.no/*/node/*", "https://ndla.no/node/*")
    val httpsNdlaEndpoint = OEmbedEndpoint(Some(httpsNdlaApprovedUrls), Some(OEmbedProxyProperties.HttpsNdlaOembedServiceUrl), None, None)
    val httpsNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(httpsNdlaEndpoint), url => url.removeAllParams())

    val youtubeEndpoint = OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), None, None)
    val youTubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(youtubeEndpoint))
    val youtuProvider = OEmbedProvider("YouTube", "http://youtu.be", List(youtubeEndpoint))

    def loadProviders(): List[OEmbedProvider] = {
      httpNdlaProvider :: httpsNdlaProvider :: youtuProvider :: goOpenProvider :: loadProvidersFromRequest(Http(OEmbedProxyProperties.JSonProviderUrl))
    }

    def loadProvidersFromRequest(request: HttpRequest): List[OEmbedProvider] = {
      val providersTry = ndlaClient.fetch[List[OEmbedProvider]](request)
      providersTry match {
        // Only keep providers with at least one endpoint with at least one url
        case Success(providers) => providers.filter(_.endpoints.nonEmpty).filter(_.endpoints.forall(endpoint => endpoint.url.isDefined))
        case Failure(ex) => {
          logger.warn(ex.getMessage)
          List(youTubeProvider)
        }
      }
    }
  }
}

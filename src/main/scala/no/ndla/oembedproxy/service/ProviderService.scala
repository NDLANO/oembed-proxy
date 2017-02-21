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

    val GoOpenEndpoint = OEmbedEndpoint(None, Some("http://www.goopen.no/"), None, None, List("oembed=true"))
    val GoOpenProvider = OEmbedProvider("GoOpen.no", "http://www.goopen.no", GoOpenEndpoint :: Nil)

    val HttpNdlaApprovedUrls = List("http://ndla.no/*/node/*", "http://ndla.no/node/*")
    val HttpNdlaEndpoint = OEmbedEndpoint(Some(HttpNdlaApprovedUrls), Some("http://ndla.no/services/oembed"), None, None)
    val HttpNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpNdlaEndpoint), url => url.removeAllParams())

    val HttpsNdlaApprovedUrls = List("https://ndla.no/*/node/*", "https://ndla.no/node/*")
    val HttpsNdlaEndpoint = OEmbedEndpoint(Some(HttpsNdlaApprovedUrls), Some("https://ndla.no/services/oembed"), None, None)
    val HttpsNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpsNdlaEndpoint), url => url.removeAllParams())

    val YoutubeEndpoint = OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), None, None)
    val YouTubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(YoutubeEndpoint))
    val YoutuProvider = OEmbedProvider("YouTube", "http://youtu.be", List(YoutubeEndpoint))

    val H5PEndpoint = OEmbedEndpoint(None, Some("https://ndlah5p.joubel.com/h5p-oembed.json"), None, None)
    val H5PProvider = OEmbedProvider("ndlah5p.joubel.com", "https://ndlah5p.joubel.com", List(H5PEndpoint))

    val NdlaApiApprovedUrls = List(OEmbedProxyProperties.NdlaApprovedUrl)
    val NdlaApiEndpoint = OEmbedEndpoint(Some(NdlaApiApprovedUrls), Some(OEmbedProxyProperties.NdlaApiOembedServiceUrl), None, None)
    val NdlaApiProvider = OEmbedProvider("NDLA Api", OEmbedProxyProperties.NdlaApiOembedProvider, List(NdlaApiEndpoint), url => url.removeAllParams())

    val TedApprovedUrls = List("https://www.ted.com/talks/*")
    val TedEndpoint = OEmbedEndpoint(Some(TedApprovedUrls), Some("https://www.ted.com/talks/oembed.json"), None, None)
    val TedProvider = OEmbedProvider("Ted", "https://ted.com", List(TedEndpoint), url => url.removeAllParams())

    def loadProviders(): List[OEmbedProvider] = {
      NdlaApiProvider :: TedProvider :: H5PProvider :: HttpNdlaProvider :: HttpsNdlaProvider :: YoutuProvider :: GoOpenProvider :: loadProvidersFromRequest(Http(OEmbedProxyProperties.JSonProviderUrl))
    }

    def loadProvidersFromRequest(request: HttpRequest): List[OEmbedProvider] = {
      val providersTry = ndlaClient.fetch[List[OEmbedProvider]](request)
      providersTry match {
        // Only keep providers with at least one endpoint with at least one url
        case Success(providers) => providers.filter(_.endpoints.nonEmpty).filter(_.endpoints.forall(endpoint => endpoint.url.isDefined))
        case Failure(ex) => {
          logger.warn(ex.getMessage)
          List(YouTubeProvider)
        }
      }
    }
  }
}

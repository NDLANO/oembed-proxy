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
    val HttpNdlaEndpoint = OEmbedEndpoint(Some(HttpNdlaApprovedUrls), Some(OEmbedProxyProperties.HttpNdlaOembedServiceUrl), None, None)
    val HttpNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpNdlaEndpoint), url => url.removeAllParams())

    val HttpsNdlaApprovedUrls = List("https://ndla.no/*/node/*", "https://ndla.no/node/*")
    val HttpsNdlaEndpoint = OEmbedEndpoint(Some(HttpsNdlaApprovedUrls), Some(OEmbedProxyProperties.HttpsNdlaOembedServiceUrl), None, None)
    val HttpsNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpsNdlaEndpoint), url => url.removeAllParams())

    val YoutubeEndpoint = OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), None, None)
    val YouTubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(YoutubeEndpoint))
    val YoutuProvider = OEmbedProvider("YouTube", "http://youtu.be", List(YoutubeEndpoint))

    val H5PEndpoint = OEmbedEndpoint(None, Some("https://ndlah5p.joubel.com/h5p-oembed.json"), None, None)
    val H5PProvider = OEmbedProvider("ndlah5p.joubel.com", "https://ndlah5p.joubel.com", List(H5PEndpoint))

    val NdlaStagingApprovedUrls = List("http://api.staging.ndla.no:8082/article/*")
    val NdlaStagingEndpoint = OEmbedEndpoint(Some(NdlaStagingApprovedUrls), Some(OEmbedProxyProperties.NdlaStagingOembedServiceUrl), None, None)
    val NdlaStagingProvider = OEmbedProvider("api.staging.ndla.no", "http://api.staging.ndla.no", List(NdlaStagingEndpoint), url => url.removeAllParams())

    val NdlaTestApprovedUrls = List("http://api.test.ndla.no:8082/article/*")
    val NdlaTestEndpoint = OEmbedEndpoint(Some(NdlaTestApprovedUrls), Some(OEmbedProxyProperties.NdlaTestOembedServiceUrl), None, None)
    val NdlaTestProvider = OEmbedProvider("api.test.ndla.no", "http://api.test.ndla.no", List(NdlaTestEndpoint), url => url.removeAllParams())

    def loadProviders(): List[OEmbedProvider] = {
      NdlaTestProvider :: NdlaStagingProvider :: H5PProvider :: HttpNdlaProvider :: HttpsNdlaProvider :: YoutuProvider :: GoOpenProvider :: loadProvidersFromRequest(Http(OEmbedProxyProperties.JSonProviderUrl))
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

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
import no.ndla.oembedproxy.OEmbedProxyProperties
import no.ndla.oembedproxy.caching.Memoize
import no.ndla.oembedproxy.model.{DoNotUpdateMemoizeException, OEmbedEndpoint, OEmbedProvider}
import no.ndla.oembedproxy.service.OEmbedConverterService.{
  addYoutubeTimestampIfdefinedInRequest,
  removeQueryString,
  removeQueryStringAndFragment,
  handleYoutubeRequestUrl
}
import org.json4s.DefaultFormats

import scala.util.{Failure, Success}
import scalaj.http.{Http, HttpRequest}

trait ProviderService {
  this: NdlaClient =>
  val providerService: ProviderService

  class ProviderService extends LazyLogging {
    implicit val formats: DefaultFormats = org.json4s.DefaultFormats

    val GoOpenEndpoint = OEmbedEndpoint(None, Some("http://www.goopen.no/"), None, None, List(("oembed", "true")))

    val GoOpenProvider =
      OEmbedProvider("GoOpen.no", "http://www.goopen.no", GoOpenEndpoint :: Nil)

    val HttpNdlaApprovedUrls =
      List("http://ndla.no/*/node/*", "http://ndla.no/node/*")

    val HttpNdlaEndpoint =
      OEmbedEndpoint(Some(HttpNdlaApprovedUrls), Some("http://ndla.no/services/oembed"), None, None)
    val HttpNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpNdlaEndpoint), removeQueryString)

    val HttpsNdlaApprovedUrls =
      List("https://ndla.no/*/node/*", "https://ndla.no/node/*")

    val HttpsNdlaEndpoint =
      OEmbedEndpoint(Some(HttpsNdlaApprovedUrls), Some("https://ndla.no/services/oembed"), None, None)
    val HttpsNdlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(HttpsNdlaEndpoint), removeQueryString)

    val NdlaApiApprovedUrls = OEmbedProxyProperties.NdlaApprovedUrl

    val NdlaApiEndpoint =
      OEmbedEndpoint(Some(NdlaApiApprovedUrls), Some(OEmbedProxyProperties.NdlaApiOembedServiceUrl), None, None)

    val NdlaApiProvider =
      OEmbedProvider("NDLA Api", OEmbedProxyProperties.NdlaApiOembedProvider, List(NdlaApiEndpoint), removeQueryString)

    val ListingFrontendEndpoint = OEmbedEndpoint(Some(OEmbedProxyProperties.ListingFrontendApprovedUrls),
                                                 Some(OEmbedProxyProperties.ListingFrontendOembedServiceUrl),
                                                 None,
                                                 None)

    val ListingFrontendProvider =
      OEmbedProvider("NDLA Liste", "https://liste.ndla.no", List(ListingFrontendEndpoint))

    val YoutubeEndpoint =
      OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), None, None)

    val YoutuProvider = OEmbedProvider("YouTube",
                                       "http://youtu.be",
                                       List(YoutubeEndpoint),
                                       handleYoutubeRequestUrl,
                                       addYoutubeTimestampIfdefinedInRequest)

    val YoutubeProvider = OEmbedProvider("YouTube",
                                         "http://www.youtube.com",
                                         List(YoutubeEndpoint),
                                         handleYoutubeRequestUrl,
                                         addYoutubeTimestampIfdefinedInRequest)

    val H5PApprovedUrls = List(OEmbedProxyProperties.NdlaH5PApprovedUrl)

    val H5PEndpoint =
      OEmbedEndpoint(Some(H5PApprovedUrls), Some(s"${OEmbedProxyProperties.NdlaH5POembedProvider}/oembed"), None, None)
    val H5PProvider = OEmbedProvider("H5P", OEmbedProxyProperties.NdlaH5POembedProvider, List(H5PEndpoint))

    val TedApprovedUrls = List(
      "https://www.ted.com/talks/*",
      "http://www.ted.com/talks/*",
      "https://ted.com/talks/*",
      "http://ted.com/talks/*",
      "www.ted.com/talks/*",
      "ted.com/talks/*",
      "https://www.embed.ted.com/talks/*",
      "http://www.embed.ted.com/talks/*",
      "https://embed.ted.com/talks/*",
      "http://embed.ted.com/talks/*",
      "www.embed.ted.com/talks/*",
      "embed.ted.com/talks/*"
    )

    val TedEndpoint =
      OEmbedEndpoint(Some(TedApprovedUrls), Some("https://www.ted.com/services/v1/oembed.json"), None, None)
    val TedProvider = OEmbedProvider("Ted", "https://ted.com", List(TedEndpoint), removeQueryString)

    val IssuuApprovedUrls = List("http://issuu.com/*", "https://issuu.com/*")

    val IssuuEndpoint =
      OEmbedEndpoint(Some(IssuuApprovedUrls), Some("https://issuu.com/oembed"), None, None, List(("iframe", "true")))
    val IssuuProvider = OEmbedProvider("Issuu", "https://issuu.com", List(IssuuEndpoint), removeQueryStringAndFragment)

    val loadProviders = Memoize(() => {
      logger.info("Provider cache was not found or out of date, fetching providers")
      _loadProviders()
    })

    def _loadProviders(): List[OEmbedProvider] = {
      GoOpenProvider ::
        H5PProvider ::
        HttpNdlaProvider ::
        HttpsNdlaProvider ::
        IssuuProvider ::
        ListingFrontendProvider ::
        NdlaApiProvider ::
        TedProvider ::
        YoutuProvider ::
        YoutubeProvider ::
        loadProvidersFromRequest(Http(OEmbedProxyProperties.JSonProviderUrl))
    }

    def loadProvidersFromRequest(request: HttpRequest): List[OEmbedProvider] = {
      val providersTry = ndlaClient.fetch[List[OEmbedProvider]](request)
      providersTry match {
        // Only keep providers with at least one endpoint with at least one url
        case Success(providers) =>
          providers
            .filter(_.endpoints.nonEmpty)
            .filter(_.endpoints.forall(endpoint => endpoint.url.isDefined))
        case Failure(ex) =>
          logger.error(s"Failed to load providers from ${request.url}.")
          throw new DoNotUpdateMemoizeException(ex.getMessage)
      }
    }
  }
}

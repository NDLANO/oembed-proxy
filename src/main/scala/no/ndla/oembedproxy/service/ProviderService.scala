package no.ndla.oembedproxy.service

import com.typesafe.scalalogging.LazyLogging
import no.ndla.oembedproxy.OEmbedProxyProperties
import no.ndla.oembedproxy.model.{OEmbedEndpoint, OEmbedProvider}
import org.json4s.native.JsonMethods._

import scalaj.http.{Http, HttpRequest}
import com.netaporter.uri.dsl._

object ProviderLoader {
  def loadProviders = new ProviderService().loadProviders()
}

class ProviderService extends LazyLogging {
  implicit val formats = org.json4s.DefaultFormats

  val goOpenEndpoint = OEmbedEndpoint(None, Some("http://www.goopen.no/"), None, None, List("oembed=true"))
  val goOpenProvider = OEmbedProvider("GoOpen.no", "http://www.goopen.no", goOpenEndpoint :: Nil)

  val ndlaApprovedUrls = List(
    "http://ndla.no/*/node/*", "https://ndla.no/*/node/*",
    "http://ndla.no/node/*", "https://ndla.no/node/*")
  val ndlaEndpoint = OEmbedEndpoint(Some(ndlaApprovedUrls), Some(OEmbedProxyProperties.NdlaOembedServiceUrl), None, None)
  val ndlaProvider = OEmbedProvider("ndla", "http://www.ndla.no", List(ndlaEndpoint), url => url.removeAllParams())

  val youtubeEndpoint = OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), None, None)
  val youTubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(youtubeEndpoint))
  val youtuProvider = OEmbedProvider("YouTube", "http://youtu.be", List(youtubeEndpoint))

  def loadProviders(): List[OEmbedProvider] = {
    ndlaProvider :: youtuProvider :: goOpenProvider :: loadProvidersFromRequest(Http(OEmbedProxyProperties.JSonProviderUrl))
  }

  def loadProvidersFromRequest(request: HttpRequest): List[OEmbedProvider] = {
    val response = request.asString
    response.isError match {
      case true => {
        logger.warn(s"Received http error ${response.code} - ${response.statusLine} when trying to load providers from url ${request.url}. Defaulting to only NDLA and Youtube.")
        List(youTubeProvider)
      }
      case false => parseJson(response.body)
    }
  }

  private def parseJson(json: String): List[OEmbedProvider] = {
    try {
      val providers = parse(json).camelizeKeys.extract[List[OEmbedProvider]]
      providers
        .filter(_.endpoints.nonEmpty) // Only keep providers with at least one endpoint
        .filter(_.endpoints.forall(endpoint => endpoint.url.isDefined)) // Require that the endpoint has at least url

    } catch {
      case e: Exception => {
        logger.warn(s"Could not parse $json. Defaulting to only NDLA, Youtube and GoOpen as oEmbed-providers.")
        List(youTubeProvider)
      }
    }
  }
}

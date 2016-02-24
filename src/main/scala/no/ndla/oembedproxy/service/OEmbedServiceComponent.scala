package no.ndla.oembedproxy.service

import com.typesafe.scalalogging.LazyLogging
import no.ndla.oembedproxy.model.{HttpRequestException, ProviderNotSupportedException, OEmbedProvider, OEmbed}
import org.json4s.native.JsonMethods._

import scala.util.Try
import scalaj.http.{Http, HttpRequest}


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
      get(Http(provider.requestUrl(url, maxWidth, maxHeight)))
    }

    def get(request: HttpRequest): OEmbed = {
      val response = request.asString
      response.isError match {
        case true => throw new HttpRequestException(s"Got ${response.code} ${response.statusLine} when calling ${request.url}")
        case false => {
          Try {parse(response.body).camelizeKeys.extract[OEmbed]}.toOption match {
            case Some(x) => x
            case None => throw new HttpRequestException(s"Unreadable response from ${request.url}")
          }
        }
      }
    }
  }
}

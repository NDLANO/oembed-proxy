package no.ndla.oembedproxy.controller

import com.typesafe.scalalogging.LazyLogging
import no.ndla.logging.LoggerContext
import no.ndla.network.ApplicationUrl
import no.ndla.oembedproxy.ComponentRegistry
import no.ndla.oembedproxy.model._
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class OEmbedProxyController(implicit val swagger: Swagger) extends ScalatraServlet with NativeJsonSupport with SwaggerSupport with LazyLogging {
  protected implicit override val jsonFormats: Formats = DefaultFormats + new EnumNameSerializer(Error)

  protected val applicationDescription = "API for accessing Learningpaths from ndla.no."

  val oEmbed =
    (apiOperation[OEmbed]("oembed")
      summary "Returns eEmbed information for a given url"
      notes "Returns eEmbed information for a given url"
      parameters(
      headerParam[Option[String]]("X-Correlation-ID").description("User supplied correlation-id. May be omitted."),
      headerParam[Option[String]]("app-key").description("Your app-key."),
      queryParam[String]("url").description("The URL to retrieve embedding information for"),
      queryParam[Option[String]]("maxwidth").description("The maximum width of the embedded resource"),
      queryParam[Option[String]]("maxheight").description("The maximum height of the embedded resource")
      ))

  before() {
    contentType = formats("json")
    LoggerContext.setCorrelationID(Option(request.getHeader("X-Correlation-ID")))
    ApplicationUrl.set(request)
  }

  after() {
    LoggerContext.clearCorrelationID()
    ApplicationUrl.clear()
  }

  error{
    case pme:ParameterMissingException => halt(status = 400, body = Error(Error.PARAMETER_MISSING, pme.getMessage))
    case pnse:ProviderNotSupportedException => halt(status = 501, body = Error(Error.PROVIDER_NOT_SUPPORTED, pnse.getMessage))
    case hre: HttpRequestException => halt(status = 502, body = Error(Error.REMOTE_ERROR, hre.getMessage))
    case t:Throwable => {
      t.printStackTrace()
      logger.error(t.getMessage)
      halt(status = 500, body = Error.GenericError)
    }
  }

  val oEmbedService = ComponentRegistry.oEmbedService

  get("/", operation(oEmbed)) {
    val urlOpt = params.get("url")
    val maxWidth = params.get("maxwidth")
    val maxHeight = params.get("maxheight")

    urlOpt match {
      case None => throw new ParameterMissingException("The required parameter 'url' is missing.")
      case Some(url) => {
        logger.info(s"GET / with params url='$url', maxwidth='$maxWidth', maxheight='$maxHeight'")
        oEmbedService.get(url, maxWidth, maxHeight)
      }
    }
  }
}

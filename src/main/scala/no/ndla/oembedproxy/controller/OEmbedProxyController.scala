/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.controller

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.ApplicationUrl
import no.ndla.network.model.HttpRequestException
import no.ndla.oembedproxy.model._
import no.ndla.oembedproxy.service.OEmbedServiceComponent
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{ResponseMessage, Swagger, SwaggerSupport}
import org.scalatra.util.NotNothing

import scala.util.{Failure, Success}

trait OEmbedProxyController {
  this: OEmbedServiceComponent =>
  val oEmbedProxyController: OEmbedProxyController

  class OEmbedProxyController(implicit val swagger: Swagger)
      extends ScalatraServlet
      with NativeJsonSupport
      with SwaggerSupport
      with LazyLogging
      with CorrelationIdSupport {
    protected implicit override val jsonFormats: Formats = DefaultFormats

    protected val applicationDescription =
      "API for accessing Learningpaths from ndla.no."

    registerModel[Error]()

    val response400 = ResponseMessage(400, "Validation error", Some("Error"))
    val response401 = ResponseMessage(401, "Unauthorized")
    val response500 = ResponseMessage(500, "Unknown error", Some("Error"))
    val response501 =
      ResponseMessage(501, "Provider Not Supported", Some("Error"))
    val response502 = ResponseMessage(502, "Bad Gateway", Some("Error"))

    case class Param(paramName: String, description: String)

    private val correlationId =
      Param("X-Correlation-ID", "User supplied correlation-id. May be omitted.")
    private val urlParam =
      Param("url", "The URL to retrieve embedding information for")
    private val maxWidth =
      Param("maxwidth", "The maximum width of the embedded resource")
    private val maxHeight =
      Param("maxheight", "The maximum height of the embedded resource")

    protected def asQueryParam[T: Manifest: NotNothing](param: Param) =
      queryParam[T](param.paramName).description(param.description)
    protected def asHeaderParam[T: Manifest: NotNothing](param: Param) =
      headerParam[T](param.paramName).description(param.description)

    before() {
      contentType = formats("json")
      ApplicationUrl.set(request)
      logger.info("{} {}{}",
                  request.getMethod,
                  request.getRequestURI,
                  Option(request.getQueryString).map(s => s"?$s").getOrElse(""))
    }

    after() {
      ApplicationUrl.clear()
    }

    error {
      case pme: ParameterMissingException =>
        halt(status = 400,
             body = Error(Error.PARAMETER_MISSING, pme.getMessage))
      case pnse: ProviderNotSupportedException =>
        halt(status = 501,
             body = Error(Error.PROVIDER_NOT_SUPPORTED, pnse.getMessage))
      case hre: HttpRequestException =>
        halt(status = 502, body = Error(Error.REMOTE_ERROR, hre.getMessage))
      case t: Throwable => {
        t.printStackTrace()
        logger.error(t.getMessage)
        halt(status = 500, body = Error.GenericError)
      }
    }

    val oEmbed =
      (apiOperation[OEmbed]("oembed")
        summary "Returns oEmbed information for a given url"
        notes "Returns oEmbed information for a given url"
        parameters (
          asHeaderParam[Option[String]](correlationId),
          asQueryParam[String](urlParam),
          asQueryParam[Option[String]](maxWidth),
          asQueryParam[Option[String]](maxHeight)
      )
        authorizations "oauth2"
        responseMessages (response400, response401, response500, response501, response502))

    get("/", operation(oEmbed)) {
      val maxWidth = params.get(this.maxWidth.paramName)
      val maxHeight = params.get(this.maxHeight.paramName)

      params.get(urlParam.paramName) match {
        case None =>
          throw new ParameterMissingException(
            s"The required parameter '${urlParam.paramName}' is missing.")
        case Some(url) =>
          oEmbedService.get(url, maxWidth, maxHeight) match {
            case Success(oembed) => oembed
            case Failure(ex)     => throw ex
          }
      }
    }
  }

}

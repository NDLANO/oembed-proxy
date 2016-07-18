/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, NativeSwaggerBase, Swagger}

/**
 * Created by kes on 24/02/16.
 */
class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase

object OEmbedProxyInfo {
  val apiInfo = ApiInfo(
    "OEmbed Proxy",
    "Documentation for the OEmbed Proxy of NDLA.no",
    "http://ndla.no",
    OEmbedProxyProperties.ContactEmail,
    "GPL v3.0",
    "http://www.gnu.org/licenses/gpl-3.0.en.html")
}

class OEmbedSwagger extends Swagger(Swagger.SpecVersion, "0.8", OEmbedProxyInfo.apiInfo)
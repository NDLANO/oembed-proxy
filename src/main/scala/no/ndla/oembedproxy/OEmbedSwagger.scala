/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import org.scalatra.ScalatraServlet
import org.scalatra.swagger._

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase {
  get("/") {
    renderSwagger2(swagger.docs.toList)
  }
}

object OEmbedProxyInfo {
  val apiInfo = ApiInfo(
    "OEmbed Proxy",
    "Documentation for the OEmbed Proxy of NDLA.no",
    "http://ndla.no",
    OEmbedProxyProperties.ContactEmail,
    "GPL v3.0",
    "http://www.gnu.org/licenses/gpl-3.0.en.html")
}

class OEmbedSwagger extends Swagger("2.0", "0.8", OEmbedProxyInfo.apiInfo){
  addAuthorization(OAuth(List("oembed:all"), List(ApplicationGrant(TokenEndpoint("/auth/tokens", "access_token")))))
}
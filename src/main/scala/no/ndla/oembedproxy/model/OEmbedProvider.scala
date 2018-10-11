/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.model

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.dsl._

case class OEmbedProvider(providerName: String,
                          providerUrl: String,
                          endpoints: List[OEmbedEndpoint],
                          urlParser: String => String = identity,
                          postProcessor: (String, OEmbed) => OEmbed = (_: String, o: OEmbed) => o) {

  def supports(url: String): Boolean = {
    endpoints.exists(_.supports(url)) || hostMatches(url)
  }

  def hostMatches(url: String): Boolean = {
    url.hostOption.exists(providerUrl.hostOption.contains)
  }

  private def _requestUrl(url: String, maxWidth: Option[String], maxHeight: Option[String]): String = {
    endpoints.find(_.url.isDefined) match {
      case None =>
        throw new RuntimeException(s"The provider '$providerName' has no embed-url available")
      case Some(endpoint) =>
        val embedUrl = endpoint.url.get.replace("{format}", "json") // Some providers have {format} instead of ?format=
        val width = maxWidth.map(("maxwidth", _)).toList
        val height = maxHeight.map(("maxheight", _)).toList
        val params = List(("url", url), ("format", "json")) ++ endpoint.mandatoryQueryParams ++ width ++ height
        Url.parse(embedUrl).addParams(params)
    }
  }

  def requestUrl(url: String, maxWidth: Option[String], maxHeight: Option[String]): String =
    _requestUrl(urlParser(url), maxWidth, maxHeight)
}

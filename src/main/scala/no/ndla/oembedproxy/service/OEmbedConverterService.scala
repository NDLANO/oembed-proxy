/*
 * Part of NDLA oembed-proxy.
 * Copyright (C) 2018 NDLA
 *
 * See LICENSE
 */

package no.ndla.oembedproxy.service

import io.lemonlabs.uri.Url
import no.ndla.oembedproxy.model.OEmbed
import io.lemonlabs.uri.dsl._
import org.jsoup.Jsoup

object OEmbedConverterService {
  def addYoutubeTimestampIfdefinedInRequest(requestUrl: String, oembed: OEmbed): OEmbed = {
    requestUrl.query.param("start").orElse(requestUrl.query.param("time_continue")) match {
      case None => oembed
      case Some(timestamp) =>
        val newHtml = oembed.html.map(Jsoup.parseBodyFragment).map(document => {
          Option(document.select ("iframe[src]").first)
            .foreach(element => {
              val newSrcUrl = element.attr ("src").addParam ("start", timestamp).toString
              element.attr ("src", newSrcUrl.toString)
            })
          document.body().html().replaceFirst("&amp;", "&") // JSoup escapes & - even in attributes, and there is no way to disable it
        })
        oembed.copy (html = newHtml)
    }
  }
  def cleanYoutubeRequestUrl(url: String): String = filterQueryNames(url.replaceAll("&amp;", "&"), Set("v"))

  def removeQueryString(url: String): String = Url.parse(url).removeQueryString()

  def removeQueryStringAndFragment(url: String): String = Url.parse(removeQueryString(url)).withFragment(None)

  def filterQueryNames(url: String, allowedQueryParamNames: Set[String]): String = Url.parse(url).filterQueryNames(allowedQueryParamNames.contains)
}

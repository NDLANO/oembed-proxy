package no.ndla.oembedproxy.model

import com.netaporter.uri.Uri.parse


case class OEmbedProvider (providerName: String, providerUrl: String, endpoints: List[OEmbedEndpoint], urlParser: String => String = x => x) {

  def supports(url: String):Boolean = {
    endpoints.exists(_.supports(url)) || hostMatches(url)
  }

  def hostMatches(url: String): Boolean = {
    parse(url).host == parse(providerUrl).host
  }

  private def _requestUrl(url: String, maxWidth: Option[String], maxHeight: Option[String]): String = {
    endpoints.find(_.url.isDefined) match {
      case None => throw new RuntimeException(s"The provider '$providerName' has no embed-url available")
      case Some(endpoint) => {
        val embedUrl = endpoint.url.get.replace("{format}", "json")  // Some providers have {format} instead of ?format=
        val width = maxWidth.map(s => s"&maxwidth=$s").getOrElse("")
        val height = maxHeight.map(s => s"&maxheight=$s").getOrElse("")
        s"$embedUrl?url=$url$width$height&format=json"
      }
    }
  }

  def requestUrl(url: String, maxWidth: Option[String], maxHeight: Option[String]): String = _requestUrl(urlParser(url), maxWidth, maxHeight)
}


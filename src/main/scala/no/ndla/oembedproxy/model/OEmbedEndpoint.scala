package no.ndla.oembedproxy.model

case class OEmbedEndpoint (schemes: Option[List[String]], url: Option[String], discovery: Option[Boolean], formats: Option[List[String]]) {

  def supports(url: String): Boolean = {
    schemes match {
      case None => false
      case Some(schemesList) => schemesList.exists(scheme => matches(scheme, url))
    }
  }

  def matches(scheme: String, url: String): Boolean = {
    val regex = scheme.replace(".", "\\.").replace("*", ".*")
    url.matches(regex)
  }
}
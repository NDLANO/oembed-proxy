package no.ndla.oembedproxy

import org.scalatest._
import org.scalatest.mock.MockitoSugar


abstract class UnitSuite extends FunSuite with Matchers with OptionValues with Inside with Inspectors with MockitoSugar with BeforeAndAfterEach with BeforeAndAfter {
  OEmbedProxyProperties.setProperties(Map(
    "CONTACT_EMAIL" -> Some("ndla@knowit.no"),
    "HOST_ADDR" -> Some("localhost"),
    "DOMAINS" -> Some("localhost"),
    "JSON_PROVIDER_URL" -> Some("http://oembed.com/providers.json"),
    "NDLA_OEMBED_SERVICE_URL" -> Some("http://ndla.no/services/oembed"),
    "NDLACOMPONENT" -> Some("oembed-proxy")
  ))
}
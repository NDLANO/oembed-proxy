/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import org.scalatest._
import org.scalatest.mock.MockitoSugar


abstract class UnitSuite extends FunSuite with Matchers with OptionValues with Inside with Inspectors with MockitoSugar with BeforeAndAfterEach with BeforeAndAfter {
  OEmbedProxyProperties.setProperties(Map(
    "CONTACT_EMAIL" -> Some("someone@somewhere"),
    "HOST_ADDR" -> Some("localhost"),
    "JSON_PROVIDERS_URL" -> Some("http://some-url"),
    "HTTP_NDLA_OEMBED_SERVICE_URL" -> Some("http://some-other-url"),
    "HTTPS_NDLA_OEMBED_SERVICE_URL" -> Some("https://some-other-url"),
    "NDLACOMPONENT" -> Some("oembed-proxy")
  ))
}
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
    "CONTACT_EMAIL" -> Some("ndla@knowit.no"),
    "HOST_ADDR" -> Some("localhost"),
    "JSON_PROVIDERS_URL" -> Some("http://some-url"),
    "NDLA_OEMBED_SERVICE_URL" -> Some("http://some-other-url"),
    "NDLACOMPONENT" -> Some("oembed-proxy")
  ))
}
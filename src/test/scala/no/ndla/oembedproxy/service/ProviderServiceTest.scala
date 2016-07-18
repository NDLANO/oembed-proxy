/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import no.ndla.oembedproxy.UnitSuite
import org.mockito.Mockito._

import scalaj.http.{HttpRequest, HttpResponse}

class ProviderServiceTest extends UnitSuite {

  val IncompleteProvider =
    """
      |{
      |    "provider_name": "Gfycat",
      |    "provider_url": "https://gfycat.com/",
      |    "endpoints": [
      |      {
      |        "schemes": [
      |          "http://gfycat.com/*",
      |          "http://www.gfycat.com/*",
      |          "https://gfycat.com/*",
      |          "https://www.hgfycat.com/*"
      |        ]
      |      },
      |      {
      |        "url": "https://api.gfycat.com/v1/oembed",
      |        "discovery": true
      |      }
      |    ]
      |  }
    """.stripMargin

  val CompleteProvider =
    """
      |{
      |    "provider_name": "IFTTT",
      |    "provider_url": "http://www.ifttt.com/",
      |    "endpoints": [
      |      {
      |        "schemes": [
      |          "http://ifttt.com/recipes/*"
      |        ],
      |        "url": "http://www.ifttt.com/oembed/",
      |        "discovery": true
      |      }
      |    ]
      |  }
    """.stripMargin

  val BodyWithOneIncompleteProvider = s"[$IncompleteProvider]"
  val BodyWithOneCompleteProvider = s"[$CompleteProvider]"
  val BodyWithOneCompleteAndOneIncomplete = s"[$IncompleteProvider,$CompleteProvider]"

  var service: ProviderService = _

  override def beforeEach() = {
    service = new ProviderService
  }

  test("That loadProvidersFromRequest does not return an incomplete provider") {
    val httpRequest = mock[HttpRequest]
    val httpResponse = mock[HttpResponse[String]]

    when(httpRequest.asString).thenReturn(httpResponse)
    when(httpResponse.isError).thenReturn(false)
    when(httpResponse.body).thenReturn(BodyWithOneIncompleteProvider)

    val providers = service.loadProvidersFromRequest(httpRequest)
    providers.size should be(0)
  }

  test("That loadProvidersFromRequest works for a single provider") {
    val httpRequest = mock[HttpRequest]
    val httpResponse = mock[HttpResponse[String]]

    when(httpRequest.asString).thenReturn(httpResponse)
    when(httpResponse.isError).thenReturn(false)
    when(httpResponse.body).thenReturn(BodyWithOneCompleteProvider)

    val providers = service.loadProvidersFromRequest(httpRequest)
    providers.size should be(1)
  }

  test("That loadProvidersFromRequest only returns the complete provider") {
    val httpRequest = mock[HttpRequest]
    val httpResponse = mock[HttpResponse[String]]

    when(httpRequest.asString).thenReturn(httpResponse)
    when(httpResponse.isError).thenReturn(false)
    when(httpResponse.body).thenReturn(BodyWithOneCompleteAndOneIncomplete)

    val providers = service.loadProvidersFromRequest(httpRequest)
    providers.size should be(1)
  }

  test("That loadProvidersFromRequest returns youtube as provider when http-error") {
    val url = "http://ndla.no"
    val httpRequest = mock[HttpRequest]
    val httpResponse = mock[HttpResponse[String]]

    when(httpRequest.asString).thenReturn(httpResponse)
    when(httpRequest.url).thenReturn(url)
    when(httpResponse.isError).thenReturn(true)


    val providers = service.loadProvidersFromRequest(httpRequest)
    providers.size should be (1)
    providers.head.providerName should equal("YouTube")
  }

  test("That loadProvidersFromRequest returns youtube as provider when unparseable data") {
    val url = "http://ndla.no"
    val httpRequest = mock[HttpRequest]
    val httpResponse = mock[HttpResponse[String]]

    when(httpRequest.asString).thenReturn(httpResponse)
    when(httpRequest.url).thenReturn(url)
    when(httpResponse.isError).thenReturn(false)
    when(httpResponse.body).thenReturn("unparseable")


    val providers = service.loadProvidersFromRequest(httpRequest)
    providers.size should be (1)
    providers.head.providerName should equal("YouTube")
  }

}

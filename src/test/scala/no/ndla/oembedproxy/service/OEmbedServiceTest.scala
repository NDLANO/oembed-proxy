/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import no.ndla.oembedproxy.model.{HttpRequestException, ProviderNotSupportedException, OEmbedEndpoint, OEmbedProvider}
import no.ndla.oembedproxy.{TestEnvironment, UnitSuite}
import org.mockito.Mockito._
import scalaj.http.{HttpResponse, HttpRequest}

class OEmbedServiceTest extends UnitSuite with TestEnvironment {

  val ndlaProvider = OEmbedProvider("ndla", "http://ndla.no", List(OEmbedEndpoint(None, Some("http://ndla.no/services/oembed"), None, None)))
  val youtubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), Some(true), None)))
  val providers = List(ndlaProvider, youtubeProvider)

  val validOembedJson =
    """
      |{
      |  "type": "rich",
      |  "version": "1.0",
      |  "title": "A Confectioner in the UK",
      |  "providerName": "NDLA - Nasjonal digital læringsarena",
      |  "providerUrl": "http://ndla.no/",
      |  "width": 700,
      |  "height": 800,
      |  "html": "<iframe src='http://ndla.no/en/node/128905/oembed' allowfullscreen></iframe>"
      |}
    """.stripMargin

  var service: OEmbedService = _

  override def beforeEach() = {
    service = new OEmbedService(providers)
  }

  test("That get throws ProviderNotSupportedException when no providers support the url") {
    assertResult("Could not find an oembed-provider for the url 'ABC'") {
      intercept[ProviderNotSupportedException]{
        service.get(url = "ABC", None, None)
      }.getMessage
    }
  }

  test("That get throws HttpRequestException when receiving http error") {
    val request = mock[HttpRequest]
    val response = mock[HttpResponse[String]]

    when(request.asString).thenReturn(response)
    when(request.url).thenReturn("ABC")

    when(response.isError).thenReturn(true)
    when(response.code).thenReturn(123)
    when(response.statusLine).thenReturn("Ugyldig")

    assertResult("Got 123 Ugyldig when calling ABC"){
      intercept[HttpRequestException]{service.get(request)}.getMessage
    }
  }

  test("That get throws HttpRequestException when unable to parse result") {
    val request = mock[HttpRequest]
    val response = mock[HttpResponse[String]]

    when(request.asString).thenReturn(response)
    when(request.url).thenReturn("ABC")

    when(response.isError).thenReturn(false)
    when(response.body).thenReturn("This cannot be parsed as json")

    assertResult("Unreadable response from ABC"){
      intercept[HttpRequestException]{service.get(request)}.getMessage
    }
  }

  test("That get returns an oEmbed result when able to parse correctly") {
    val request = mock[HttpRequest]
    val response = mock[HttpResponse[String]]

    when(request.asString).thenReturn(response)
    when(response.isError).thenReturn(false)
    when(response.body).thenReturn(validOembedJson)

    val oembed = service.get(request)
    oembed.`type` should equal("rich")
    oembed.title.getOrElse("") should equal("A Confectioner in the UK")
  }

}

/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.model

import no.ndla.oembedproxy.UnitSuite

class OEmbedProviderTest extends UnitSuite {

  val youtubeProvider =
    OEmbedProvider("youtube", "http://www.youtube.com", List())
  val ndlaEndpoint =
    OEmbedEndpoint(Some(List("http://www.ndla.no/*/123")), None, None, None)
  val youtubeEndpoint =
    OEmbedEndpoint(Some(List("http://www.youtube.com/*")), None, None, None)

  val goOpenEndpoint = OEmbedEndpoint(None,
                                      Some("http://www.goopen.no/"),
                                      None,
                                      None,
                                      List(("oembed", "true")))
  val goOpenProvider =
    OEmbedProvider("GoOpen.no", "http://www.goopen.no", goOpenEndpoint :: Nil)

  test("That hostMatches returns true for same host, regardless of protocol") {
    youtubeProvider.hostMatches("https://www.youtube.com") should be(
      right = true)
  }

  test("That hostMatches returns false for different hosts") {
    youtubeProvider.hostMatches("https://www.ted.com") should be(right = false)
  }

  test("That supports returns true when host matches") {
    youtubeProvider.supports("http://www.youtube.com") should be(right = true)
  }

  test("That supports returns true when endpoints matches") {
    youtubeProvider
      .copy(endpoints = List(ndlaEndpoint))
      .supports("http://www.ndla.no/nb/123") should be(right = true)
  }

  test("That support returns false when neither endpoints or host matches") {
    youtubeProvider
      .copy(endpoints = List(youtubeEndpoint))
      .supports("http://www.ndla.no/nb/123") should be(right = false)
  }

  test(
    "That requestUrl throws exception when no endpoints have embedUrl defined") {
    assertResult("The provider 'youtube' has no embed-url available") {
      intercept[RuntimeException] {
        youtubeProvider.requestUrl("random", None, None)
      }.getMessage
    }
  }

  test("That {format} is replaced in embedUrl") {
    val endpoint =
      youtubeEndpoint.copy(url = Some("http://www.youtube.com/oembed.{format}"))
    val requestUrl = youtubeProvider
      .copy(endpoints = List(endpoint))
      .requestUrl("ABC", None, None)
    requestUrl should equal(
      "http://www.youtube.com/oembed.json?url=ABC&format=json")
  }

  test("That maxwidth is appended correctly") {
    val endpoint = youtubeEndpoint.copy(url = Some("http://youtube.com/oembed"))
    val requestUrl = youtubeProvider
      .copy(endpoints = List(endpoint))
      .requestUrl("ABC", Some("100"), None)
    requestUrl should equal(
      "http://youtube.com/oembed?url=ABC&format=json&maxwidth=100")
  }

  test("That maxheight is appended correctly") {
    val endpoint = youtubeEndpoint.copy(url = Some("http://youtube.com/oembed"))
    val requestUrl = youtubeProvider
      .copy(endpoints = List(endpoint))
      .requestUrl("ABC", None, Some("100"))
    requestUrl should equal(
      "http://youtube.com/oembed?url=ABC&format=json&maxheight=100")
  }

  test("That both maxwidth and maxheight are appended correctly") {
    val endpoint = youtubeEndpoint.copy(url = Some("http://youtube.com/oembed"))
    val requestUrl = youtubeProvider
      .copy(endpoints = List(endpoint))
      .requestUrl("ABC", Some("100"), Some("200"))
    requestUrl should equal(
      "http://youtube.com/oembed?url=ABC&format=json&maxwidth=100&maxheight=200")
  }

  test("That mandatoryQueryParams are added when they are defined") {
    val toOembed =
      "http://www.goopen.no/the-true-pioneers-of-the-sharing-economy"
    val expectedRequestUrl =
      "http://www.goopen.no/?url=http://www.goopen.no/the-true-pioneers-of-the-sharing-economy&format=json&oembed=true"
    goOpenProvider.requestUrl(toOembed, None, None) should equal(
      expectedRequestUrl)
  }
}

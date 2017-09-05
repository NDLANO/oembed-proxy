/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy.service

import no.ndla.network.model.HttpRequestException
import no.ndla.oembedproxy.model._
import no.ndla.oembedproxy.{TestEnvironment, UnitSuite}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.TryValues._

import scala.util.{Failure, Success}
import scalaj.http.HttpRequest

class OEmbedServiceTest extends UnitSuite with TestEnvironment {
  val ndlaProvider = OEmbedProvider("ndla", "http://ndla.no", List(OEmbedEndpoint(None, Some("http://ndla.no/services/oembed"), None, None)))
  val youtubeProvider = OEmbedProvider("YouTube", "http://www.youtube.com/", List(OEmbedEndpoint(None, Some("http://www.youtube.com/oembed"), Some(true), None)))

  val OEmbedResponse = OEmbed("rich",
    "1.0",
    Some("A Confectioner in the UK"),
    None, None, None,
    Some("NDLA - Nasjonal digital læringsarene"),
    Some("http://ndla.no"),
    None, None, None, None, None, None, None,
    Some("<iframe src='http://ndla.no/en/node/128905/oembed' allowfullscreen></iframe>"))

  override val oEmbedService = new OEmbedService(List(ndlaProvider, youtubeProvider))

  test("That get throws ProviderNotSupportedException when no providers support the url") {
    assertResult("Could not find an oembed-provider for the url 'ABC'") {
      intercept[ProviderNotSupportedException]{
        oEmbedService.get(url = "ABC", None, None)
      }.getMessage
    }
  }

  test("That get returns a failure with HttpRequestException when receiving http error") {
    when(ndlaClient.fetch[OEmbed](any[HttpRequest])(any[Manifest[OEmbed]])).thenReturn(Failure(new HttpRequestException("An error occured")))
    val oembedTry = oEmbedService.get("http://www.youtube.com/abc", None, None)
    oembedTry should be a 'failure
    oembedTry.failure.exception.getMessage should equal ("An error occured")
  }

  test("That get returns a Success with an oEmbed when http call is successful") {
    when(ndlaClient.fetch[OEmbed](any[HttpRequest])(any[Manifest[OEmbed]])).thenReturn(Success(OEmbedResponse))
    val oembedTry = oEmbedService.get("http://ndla.no/abc", None, None)
    oembedTry should be a 'success
    oembedTry.get.`type` should equal ("rich")
    oembedTry.get.title.getOrElse("") should equal ("A Confectioner in the UK")
  }

}

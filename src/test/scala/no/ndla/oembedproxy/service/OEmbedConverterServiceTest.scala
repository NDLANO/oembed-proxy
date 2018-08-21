/*
 * Part of NDLA oembed-proxy.
 * Copyright (C) 2018 NDLA
 *
 * See LICENSE
 */

package no.ndla.oembedproxy.service

import no.ndla.oembedproxy.model.OEmbed
import no.ndla.oembedproxy.{TestEnvironment, UnitSuite}

class OEmbedConverterServiceTest extends UnitSuite with TestEnvironment {
  test(
    "a start timestamp should be added on youtube urls if defined in request url") {
    val requestUrlWIthTimeContinue =
      "https://www.youtube.com/watch?time_continue=43&amp;v=vZCsuV7Rb_w"
    val requestUrlWithStart =
      "https://www.youtube.com/watch?start=43&v=vZCsuV7Rb_w"
    val requestUrlWithtoutTimestamp =
      "https://www.youtube.com/watch?v=vZCsuV7Rb_w"
    val oembed = OEmbed(
      "video",
      "1.0",
      Some("ESS® expandable sand screen"),
      None,
      None,
      None,
      Some("Youtube"),
      Some("https://www.youtube.com"),
      None,
      None,
      None,
      None,
      None,
      None,
      None,
      Some(
        """<iframe width="459" height="344" src="https://www.youtube.com/embed/vZCsuV7Rb_w?feature=oembed" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>""")
    )
    val expectedResult = Some(
      """<iframe width="459" height="344" src="https://www.youtube.com/embed/vZCsuV7Rb_w?feature=oembed&start=43" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>""")

    OEmbedConverterService
      .addYoutubeTimestampIfdefinedInRequest(requestUrlWIthTimeContinue, oembed)
      .html should equal(expectedResult)
    OEmbedConverterService
      .addYoutubeTimestampIfdefinedInRequest(requestUrlWithStart, oembed)
      .html should equal(expectedResult)
    OEmbedConverterService
      .addYoutubeTimestampIfdefinedInRequest(requestUrlWithtoutTimestamp,
                                             oembed)
      .html should equal(oembed.html)
  }

  test("cleanYoutubeRequestUrl should strip all query params except 'v'") {
    OEmbedConverterService.cleanYoutubeRequestUrl(
      "http://youtube.com/watch?start=1&v=123asdf") should equal(
      "http://youtube.com/watch?v=123asdf")
    OEmbedConverterService.cleanYoutubeRequestUrl(
      "http://youtube.com/watch?v=123asdf") should equal(
      "http://youtube.com/watch?v=123asdf")
    OEmbedConverterService.cleanYoutubeRequestUrl(
      "http://youtube.com/watch?v=123asdf&;amptime_continue=43") should equal(
      "http://youtube.com/watch?v=123asdf")
    OEmbedConverterService.cleanYoutubeRequestUrl("notanurl") should equal(
      "notanurl")
  }

  test("removeQueryString should remove the query string from an url") {
    OEmbedConverterService.removeQueryString(
      "https://google.com?search=hoho#firsthit") should equal(
      "https://google.com#firsthit")
    OEmbedConverterService.removeQueryString("notanurl") should equal(
      "notanurl")
  }

  test(
    "removeQueryStringAndFragment should remove the query string and fragment from an url") {
    OEmbedConverterService.removeQueryStringAndFragment(
      "https://google.com?search=hoho#firsthit") should equal(
      "https://google.com")
    OEmbedConverterService.removeQueryStringAndFragment("notanurl") should equal(
      "notanurl")
  }

}

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
  test("a start timestamp should be added on youtube urls if defined in request url") {
    val requestUrlWIthTimeContinue = "https://www.youtube.com/watch?time_continue=43&amp;v=vZCsuV7Rb_w"
    val requestUrlWithStart = "https://www.youtube.com/watch?start=43&v=vZCsuV7Rb_w"
    val requestUrlWithtoutTimestamp = "https://www.youtube.com/watch?v=vZCsuV7Rb_w"
    val oembed = OEmbed("video",
      "1.0",
      Some("ESS® expandable sand screen"),
      None, None, None,
      Some("Youtube"),
      Some("https://www.youtube.com"),
      None, None, None, None, None, None, None,
      Some("""<iframe width="459" height="344" src="https://www.youtube.com/embed/vZCsuV7Rb_w?feature=oembed" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>"""))
    val expectedResult = Some("""<iframe width="459" height="344" src="https://www.youtube.com/embed/vZCsuV7Rb_w?feature=oembed&start=43" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>""")

    OEmbedConverterService.addYoutubeTimestampIfdefinedInRequest(requestUrlWIthTimeContinue, oembed).html should equal(expectedResult)
    OEmbedConverterService.addYoutubeTimestampIfdefinedInRequest(requestUrlWithStart, oembed).html should equal(expectedResult)
    OEmbedConverterService.addYoutubeTimestampIfdefinedInRequest(requestUrlWithtoutTimestamp, oembed).html should equal(oembed.html)
  }

}

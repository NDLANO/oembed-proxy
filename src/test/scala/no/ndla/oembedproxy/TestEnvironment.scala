package no.ndla.oembedproxy

import no.ndla.oembedproxy.service.OEmbedServiceComponent
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar


trait TestEnvironment extends OEmbedServiceComponent with MockitoSugar {
  val oEmbedService = mock[OEmbedService]

  def resetMocks() = {
    Mockito.reset(oEmbedService)
  }
}

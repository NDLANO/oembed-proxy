package no.ndla.oembedproxy

import no.ndla.oembedproxy.service.{OEmbedServiceComponent, ProviderLoader}


object ComponentRegistry extends OEmbedServiceComponent {
  lazy val oEmbedService = new OEmbedService(ProviderLoader.loadProviders)
}

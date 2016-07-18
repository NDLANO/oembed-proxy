/*
 * Part of NDLA oembed_proxy.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.oembedproxy

import no.ndla.oembedproxy.service.{OEmbedServiceComponent, ProviderLoader}


object ComponentRegistry extends OEmbedServiceComponent {
  lazy val oEmbedService = new OEmbedService(ProviderLoader.loadProviders)
}

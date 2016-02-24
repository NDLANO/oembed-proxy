package no.ndla.oembedproxy.model

import java.text.SimpleDateFormat
import java.util.Date

import no.ndla.oembedproxy.OEmbedProxyProperties

object Error extends Enumeration{
  val GENERIC, PARAMETER_MISSING, PROVIDER_NOT_SUPPORTED, REMOTE_ERROR = Value
  val GenericError = Error(GENERIC, s"Ooops. Something we didn't anticipate occured. We have logged the error, and will look into it. But feel free to contact ${OEmbedProxyProperties.ContactEmail} if the error persists.")
}

case class Error(code:Error.Value, description:String, occuredAt:String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))

class ParameterMissingException(message: String) extends RuntimeException(message)
class ProviderNotSupportedException(message: String) extends RuntimeException(message)
class HttpRequestException(message: String) extends RuntimeException(message)
package pmeet.pmeetserver.common.exception

import org.springframework.web.bind.support.WebExchangeBindException
import pmeet.pmeetserver.common.ErrorCode

data class WebExchangeBindExceptionDto(
  val errorCode: ErrorCode
) {

  lateinit var violations: List<String>
  lateinit var message: String

  constructor(errorCode: ErrorCode, exception: WebExchangeBindException) : this(errorCode) {
    this.message = errorCode.getMessage()
    this.violations = exception.bindingResult.fieldErrors.map { it.defaultMessage ?: "Unknown error" }
  }
}

package pmeet.pmeetserver.common.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import pmeet.pmeetserver.common.ErrorCode

private val logger = KotlinLogging.logger {}

@Component
@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(value = [WebExchangeBindException::class])
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  suspend fun handleMethodArgumentNotValidException(exception: WebExchangeBindException): WebExchangeBindExceptionDto {
    logger.error(exception) { "Validation error: ${exception.message}" }
    return WebExchangeBindExceptionDto(ErrorCode.INVALID_PARAMETER, exception)
  }
}

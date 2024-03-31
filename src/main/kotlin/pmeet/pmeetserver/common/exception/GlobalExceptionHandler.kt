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
  suspend fun handleWebExchangeBindException(exception: WebExchangeBindException): WebExchangeBindExceptionDto {
    logger.error(exception) { "error: ${exception.message}" }
    return WebExchangeBindExceptionDto(ErrorCode.INVALID_INPUT_PARAMETER, exception)
  }

  @ExceptionHandler(value = [EntityDuplicateException::class])
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  suspend fun handleEntityDuplicateException(exception: EntityDuplicateException): ExceptionDto {
    logger.error(exception) { "error: ${exception.message}" }
    return ExceptionDto(exception.errorCode, exception.message!!)
  }

  @ExceptionHandler(value = [EntityNotFoundException::class])
  @ResponseStatus(HttpStatus.NOT_FOUND)
  suspend fun handleEntityNotFoundException(exception: EntityNotFoundException): ExceptionDto {
    logger.error(exception) { "error: ${exception.message}" }
    return ExceptionDto(exception.errorCode, exception.message!!)
  }

  @ExceptionHandler(value = [UnauthorizedException::class])
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  suspend fun handleUnauthorizedExceptionException(exception: UnauthorizedException): ExceptionDto {
    logger.error(exception) { "error: ${exception.message}" }
    return ExceptionDto(exception.errorCode, exception.message!!)
  }
}

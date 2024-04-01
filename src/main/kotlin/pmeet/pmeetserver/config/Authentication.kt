package pmeet.pmeetserver.config

import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.common.utils.jwt.JwtUtil
import pmeet.pmeetserver.user.service.UserService
import reactor.core.publisher.Mono


@Component
class JwtServerAuthenticationConverter : ServerAuthenticationConverter {
  override fun convert(exchange: ServerWebExchange?): Mono<Authentication> {
    val map: Mono<Authentication> = Mono.justOrEmpty(exchange?.request?.headers?.getFirst(HttpHeaders.AUTHORIZATION))
      .filter { it.startsWith("Bearer ") }.map { it.substring((7)) }.map { jwt -> BearerToken(jwt) }
    return map
  }
}

@Component
class JwtAuthenticationManager(private val jwtUtil: JwtUtil, private val userService: UserService) :
  ReactiveAuthenticationManager {

  override fun authenticate(authentication: Authentication?): Mono<Authentication> {
    return Mono.justOrEmpty(authentication)
      .filter { auth -> auth is BearerToken }
      .cast(BearerToken::class.java)
      .flatMap { jwt -> mono { validate(jwt) } }
      .onErrorMap { error -> InvalidBearerToken(error.message) }
  }

  private suspend fun validate(token: BearerToken): Authentication {
    if (jwtUtil.isValidToken(token)) {
      val user = userService.getUserById(userId = jwtUtil.getUserId(token))
      return UsernamePasswordAuthenticationToken(user.id, null, null)
    }

    // TODO now it does not work and it will be handled
    throw UnauthorizedException(ErrorCode.UNAUTHORIZED_TOKEN)
  }
}

class InvalidBearerToken(message: String?) : AuthenticationException(message)

class BearerToken(private val value: String) : AbstractAuthenticationToken(AuthorityUtils.NO_AUTHORITIES) {
  override fun getCredentials(): Any = value // JWT 문자열을 자격 증명으로 반환하여 메서드를 재정의
  override fun getPrincipal(): Any = value // JWT 문자열을 주체로 반환하여 메서드를 재정의
}

package pmeet.pmeetserver.common.utils.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pmeet.pmeetserver.user.domain.auth.RefreshTokenMap
import pmeet.pmeetserver.user.dto.response.UserJwtDto

@Component
class JwtUtil(
  private val refreshTokenUtil: RefreshTokenUtil,

  @Value("\${spring.jwt.secret-key}") val SECRET: String,
  @Value("\${spring.jwt.access-token.expire-seconds}") val ACCESS_TOKEN_EXPIRE_TIME: Long,
  @Value("\${spring.jwt.refresh-token.expire-seconds}") val REFRESH_TOKEN_EXPIRE_TIME: Long
) {
  lateinit var SECRET_KEY: ByteArray

  @PostConstruct
  protected fun init() {
    this.SECRET_KEY = SECRET.toByteArray()
  }

  suspend fun createToken(
    userId: String
  ): UserJwtDto {
    val now = Date()
    return UserJwtDto.of(
      userId,
      generateAccessToken(userId, now),
      generateRefreshToken(userId, now)
    )
  }

  private suspend fun generateAccessToken(userId: String, now: Date): String {
    val claims: Claims = Jwts.claims().setSubject(userId)

    return Jwts.builder()
      .setClaims(claims)
      .setIssuedAt(now)
      .setExpiration(Date(now.time + ACCESS_TOKEN_EXPIRE_TIME))
      .signWith(Keys.hmacShaKeyFor(this.SECRET_KEY), io.jsonwebtoken.SignatureAlgorithm.HS256)
      .compact()
  }

  private suspend fun generateRefreshToken(userId: String, now: Date): String {
    val token: String = Jwts.builder()
      .setIssuedAt(now)
      .setExpiration(Date(now.time + REFRESH_TOKEN_EXPIRE_TIME))
      .signWith(Keys.hmacShaKeyFor(this.SECRET_KEY), io.jsonwebtoken.SignatureAlgorithm.HS256)
      .compact()

    refreshTokenUtil.save(RefreshTokenMap(token, userId))

    return token
  }
}

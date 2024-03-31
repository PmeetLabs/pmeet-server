package pmeet.pmeetserver.common.utils.jwt

import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import pmeet.pmeetserver.user.domain.auth.RefreshTokenMap

@Component
class RefreshTokenUtil(
  private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
  @Value("\${spring.jwt.refresh-token.expire-seconds}") private val REFRESH_TOKEN_EXPIRE_TIME: Long
) {

  suspend fun save(refreshTokenMap: RefreshTokenMap) {
    reactiveRedisTemplate.opsForValue()
      .set(refreshTokenMap.refreshToken, refreshTokenMap.userId, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME))
      .subscribe()
  }
}

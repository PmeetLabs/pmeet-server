package pmeet.pmeetserver.user.service

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.SignUpRequestDto
import pmeet.pmeetserver.user.dto.UserResponseDto
import pmeet.pmeetserver.user.repository.UserRepository

@Service
class UserService(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) {
  @Transactional
  suspend fun save(requestDto: SignUpRequestDto): UserResponseDto {
    userRepository.findByEmail(requestDto.email).awaitSingleOrNull()?.let {
      throw EntityDuplicateException(ErrorCode.USER_DUPLICATE_BY_EMAIL)
    }

    userRepository.findByNickname(requestDto.nickname).awaitSingleOrNull()?.let {
      throw EntityDuplicateException(ErrorCode.USER_DUPLICATE_BY_NICKNAME)
    }

    val user = userRepository.save(
      User(
        email = requestDto.email,
        name = requestDto.name,
        password = passwordEncoder.encode(requestDto.password),
        nickname = requestDto.nickname,
      )
    ).awaitSingle()

    val refreshToken = "Token" + user.id
    reactiveRedisTemplate.opsForValue().set(refreshToken, user.id!!).subscribe();

    return UserResponseDto.from(user)
  }
}

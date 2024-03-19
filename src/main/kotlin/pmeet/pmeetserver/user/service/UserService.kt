package pmeet.pmeetserver.user.service

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.SignUpRequestDto
import pmeet.pmeetserver.user.dto.UserResponseDto
import pmeet.pmeetserver.user.repository.UserRepository

@Service
class UserService(
  private val userRepository: UserRepository,
  private val passwordEncoder: PasswordEncoder
) {
  @Transactional
  suspend fun save(requestDto: SignUpRequestDto): UserResponseDto {

    val user = userRepository.save(
      User(
        email = requestDto.email,
        name = requestDto.name,
        password = passwordEncoder.encode(requestDto.password),
        nickname = requestDto.nickname,
      )
    ).awaitSingle()

    return UserResponseDto.from(user)
  }
}

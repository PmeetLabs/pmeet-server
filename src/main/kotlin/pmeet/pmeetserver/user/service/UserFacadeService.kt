package pmeet.pmeetserver.user.service

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.auth.service.EmailService
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.request.VerifyVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.response.UserResponseDto

@Service
class UserFacadeService(
  private val passwordEncoder: PasswordEncoder,
  private val userService: UserService,
  private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
  private val emailService: EmailService
) {
  @Transactional
  suspend fun save(requestDto: SignUpRequestDto): UserResponseDto {
    val user = User(
      email = requestDto.email,
      name = requestDto.name,
      password = passwordEncoder.encode(requestDto.password),
      nickname = requestDto.nickname,
    )

    return UserResponseDto.from(userService.save(user))
  }

  suspend fun signIn(requestDto: SignInRequestDto): UserResponseDto {
    val user = userService.getUserByEmail(requestDto.email)

    if (user.password != passwordEncoder.encode(requestDto.password)) {
      throw UnauthorizedException(ErrorCode.INVALID_PASSWORD)
    }

    // TODO Token Test will be deleted
//    val refreshToken = "Token" + user.id
//    reactiveRedisTemplate.opsForValue().set(refreshToken, user.id!!).subscribe();\

    return UserResponseDto.from(user)
  }

  @Transactional(readOnly = true)
  suspend fun isDuplicateNickName(requestDto: CheckNickNameRequestDto): Boolean {
    try {
      userService.getUserByNickname(requestDto.nickname).let { return true }
    } catch (e: EntityNotFoundException) {
      return false
    }
  }

  @Transactional
  suspend fun sendVerificationCode(requestDto: SendVerificationCodeRequestDto): Boolean {
    emailService.sendEmailWithVerificationCode(requestDto.email)
    return true
  }

  @Transactional(readOnly = true)
  suspend fun verifyVerificationCode(requestDto: VerifyVerificationCodeRequestDto): Boolean {
    return emailService.verifyVerificationCode(requestDto.email, requestDto.verificationCode)
  }
}


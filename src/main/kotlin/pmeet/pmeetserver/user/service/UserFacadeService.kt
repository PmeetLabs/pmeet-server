package pmeet.pmeetserver.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.auth.service.EmailService
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.common.utils.jwt.JwtUtil
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SetPasswordRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.request.VerifyVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.response.UserJwtDto
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.dto.response.UserSummaryResponseDto

@Service
class UserFacadeService(
  private val passwordEncoder: PasswordEncoder,
  private val userService: UserService,
  private val emailService: EmailService,
  private val jwtUtil: JwtUtil
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

  @Transactional
  suspend fun signIn(requestDto: SignInRequestDto): UserJwtDto {
    val user = userService.getUserByEmail(requestDto.email)

    if (!passwordEncoder.matches(requestDto.password, user.password)) {
      throw UnauthorizedException(ErrorCode.INVALID_PASSWORD)
    }

    return jwtUtil.createToken(user.id!!)
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

  @Transactional
  suspend fun setPassword(requestDto: SetPasswordRequestDto): Boolean {
    val user = userService.getUserByEmail(requestDto.email)
    user.changePassword(passwordEncoder.encode(requestDto.password))
    userService.update(user)
    return true
  }

  @Transactional
  suspend fun getMySummaryInfo(userId: String): UserSummaryResponseDto {
    return UserSummaryResponseDto.from(userService.getUserById(userId))
  }
}


package pmeet.pmeetserver.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.common.utils.jwt.JwtUtil
import pmeet.pmeetserver.file.service.FileService
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.request.CheckMailRequestDto
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SetPasswordRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.request.UpdateUserRequestDto
import pmeet.pmeetserver.user.dto.request.VerifyVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.response.UserJwtDto
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.dto.response.UserSignUpResponseDto
import pmeet.pmeetserver.user.dto.response.UserSummaryResponseDto
import pmeet.pmeetserver.user.service.mail.EmailService

@Service
class UserFacadeService(
  private val passwordEncoder: PasswordEncoder,
  private val userService: UserService,
  private val emailService: EmailService,
  private val jwtUtil: JwtUtil,
  private val fileService: FileService
) {
  @Transactional
  suspend fun save(requestDto: SignUpRequestDto): UserSignUpResponseDto {
    emailService.validateVerifiedEmail(requestDto.email)
    val user = User(
      email = requestDto.email,
      name = requestDto.name,
      password = passwordEncoder.encode(requestDto.password),
      nickname = requestDto.nickname,
    )
    return UserSignUpResponseDto.from(userService.save(user))
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
    userService.findUserByNickname(requestDto.nickname)?.let { return true }
    return false
  }

  @Transactional(readOnly = true)
  suspend fun isDuplicateMail(requestDto: CheckMailRequestDto): Boolean {
    userService.findUserByEmail(requestDto.mail)?.let { return true }
    return false
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
    userService.getUserByEmail(requestDto.email).apply {
      emailService.validateVerifiedEmail(email)
      changePassword(passwordEncoder.encode(requestDto.password))
      userService.update(this)
    }
    return true
  }

  @Transactional
  suspend fun updateUser(userId: String, requestDto: UpdateUserRequestDto): UserResponseDto {
    val user = userService.getUserById(userId).apply {
      updateUser(
        requestDto.profileImageUrl,
        requestDto.name,
        requestDto.nickname,
        requestDto.phoneNumber,
        requestDto.birthDate,
        requestDto.gender,
        requestDto.isEmployed,
        requestDto.introductionComment
      )
    }
    return UserResponseDto.of(
      userService.update(user),
      user.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional(readOnly = true)
  suspend fun getMySummaryInfo(userId: String): UserSummaryResponseDto {
    val user = userService.getUserById(userId)

    return UserSummaryResponseDto.of(
      user,
      user.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional(readOnly = true)
  suspend fun getMyInfo(userId: String): UserResponseDto {
    val user = userService.getUserById(userId)

    return UserResponseDto.of(
      user,
      user.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional
  suspend fun deleteUser(userId: String): Boolean {
    val user = userService.getUserById(userId)
    user.delete();
    userService.update(user)
    return true
  }
}


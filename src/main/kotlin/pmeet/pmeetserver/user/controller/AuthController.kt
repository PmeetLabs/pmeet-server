package pmeet.pmeetserver.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.service.UserFacadeService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
  private val userFacadeService: UserFacadeService
) {
  @PostMapping("/sign-up")
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createUser(@RequestBody @Valid requestDto: SignUpRequestDto): UserResponseDto {
    return userFacadeService.save(requestDto)
  }

  @PostMapping("/sign-in")
  @ResponseStatus(HttpStatus.OK)
  suspend fun signIn(@RequestBody @Valid requestDto: SignInRequestDto): UserResponseDto {
    return userFacadeService.signIn(requestDto)
  }

  @PostMapping("/nickname/duplicate")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getNickname(@RequestBody @Valid requestDto: CheckNickNameRequestDto): Boolean {
    return userFacadeService.isDuplicateNickName(requestDto)
  }

  @PostMapping("/verification-code")
  @ResponseStatus(HttpStatus.OK)
  suspend fun sendVerificationCode(@RequestBody @Valid requestDto: SendVerificationCodeRequestDto): Boolean {
    return userFacadeService.sendVerificationCode(requestDto)
  }
}


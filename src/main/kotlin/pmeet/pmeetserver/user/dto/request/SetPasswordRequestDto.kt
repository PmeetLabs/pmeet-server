package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import pmeet.pmeetserver.user.validation.annotation.Password
import pmeet.pmeetserver.user.validation.annotation.PasswordConfirmMatch

@PasswordConfirmMatch
data class SetPasswordRequestDto(
  @field:Email(message = "이메일 형식이 아닙니다. 다시 입력해 주세요.")
  @field:NotBlank(message = "이메일을 입력해 주세요.")
  val email: String,
  @field:Password(message = "8~16자의 영문 대소문자, 숫자, 특수문자만 가능합니다.")
  val password: String,
  @field:Password(message = "8~16자의 영문 대소문자, 숫자, 특수문자만 가능합니다.")
  val passwordConfirm: String
)

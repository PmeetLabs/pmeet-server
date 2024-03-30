package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class VerifyVerificationCodeRequestDto(
  @field:Email(message = "이메일 형식이 아닙니다. 다시 입력해 주세요.")
  @field:NotBlank(message = "이메일을 입력해 주세요.")
  val email: String,
  @field:Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
  @field:NotBlank(message = "인증번호를 입력해 주세요.")
  val verificationCode: String,
)

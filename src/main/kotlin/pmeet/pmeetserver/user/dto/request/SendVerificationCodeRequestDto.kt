package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendVerificationCodeRequestDto(
  @field:Email(message = "이메일 형식이 아닙니다. 다시 입력해 주세요.")
  @field:NotBlank(message = "이메일을 입력해 주세요.")
  val email: String,
)

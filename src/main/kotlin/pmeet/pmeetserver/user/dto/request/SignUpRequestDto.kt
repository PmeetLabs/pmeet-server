package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pmeet.pmeetserver.user.validation.annotation.Password

data class SignUpRequestDto(
  @field:Email(message = "이메일 형식이 아닙니다. 다시 입력해 주세요.")
  @field:NotBlank(message = "이메일을 입력해 주세요.")
  val email: String,
  @field:Size(min = 1, max = 30, message = "이름은 1자부터 30자까지 가능합니다.")
  @field:Pattern(regexp = "^[가-힣A-Za-z]+$", message = "이름은 문자만 입력 가능합니다.")
  val name: String,
  @field:Password(message = "8~16자의 영문 대소문자, 숫자, 특수문자만 가능합니다.")
  val password: String,
  @field:Size(min = 1, max = 30, message = "닉네임은 1자부터 30자까지 가능합니다.")
  @field:Pattern(regexp = "^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z]+\$", message = "닉네임은 문자만 입력 가능합니다.")
  val nickname: String
)

package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CheckNickNameRequestDto(
  @field:Size(min = 1, max = 30, message = "닉네임은 1자부터 30자까지 가능합니다.")
  @field:Pattern(regexp = "^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z]+\$", message = "닉네임은 문자만 입력 가능합니다.")
  val nickname: String
)


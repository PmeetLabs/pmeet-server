package pmeet.pmeetserver.user.dto.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import pmeet.pmeetserver.user.domain.enum.Gender

data class UpdateUserRequestDto(
  val profileImageUrl: String?,
  @field:Size(min = 1, max = 30, message = "이름은 1자부터 30자까지 가능합니다.")
  @field:Pattern(regexp = "^[가-힣A-Za-z]+$", message = "이름은 문자만 입력 가능합니다.")
  val name: String,
  @field:Size(min = 1, max = 30, message = "닉네임은 1자부터 30자까지 가능합니다.")
  @field:Pattern(regexp = "^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]+$", message = "닉네임은 문자만 입력 가능합니다.")
  val nickname: String,
  @field:Pattern(regexp = """^\d{3}-\d{4}-\d{4}$""", message = "올바른 전화번호 입력 형식이 아닙니다.")
  val phoneNumber: String?,
  val birthDate: LocalDate?,
  val gender: Gender?,
  val isEmployed: Boolean,
  @field:Size(max = 50, message = "한 줄 소개는 최대 50자까지 입력 가능합니다.")
  val introductionComment: String?
)

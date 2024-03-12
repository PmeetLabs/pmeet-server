package pmeet.pmeetserver.member.dto

data class SignUpRequestDto(
  val email: String, val password: String, val nickname: String
)

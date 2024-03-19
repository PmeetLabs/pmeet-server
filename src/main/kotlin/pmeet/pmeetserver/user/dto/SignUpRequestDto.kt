package pmeet.pmeetserver.user.dto

data class SignUpRequestDto(
  val email: String, val name: String, val password: String, val nickname: String
)

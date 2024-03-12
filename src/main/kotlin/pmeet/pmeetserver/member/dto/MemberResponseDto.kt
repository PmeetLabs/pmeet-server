package pmeet.pmeetserver.member.dto

data class MemberResponseDto(
  val id: String, val email: String, val password: String, val nickname: String
)

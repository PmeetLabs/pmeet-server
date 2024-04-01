package pmeet.pmeetserver.user.dto.response

import pmeet.pmeetserver.user.domain.User


data class UserSummaryResponseDto(
  val id: String,
  val email: String,
  val nickname: String,
  val isEmployed: Boolean,
  val profileImageUrl: String?
) {
  companion object {
    fun from(user: User): UserSummaryResponseDto {
      return UserSummaryResponseDto(
        id = user.id!!,
        email = user.email,
        nickname = user.nickname,
        isEmployed = user.isEmployed,
        profileImageUrl =  user.profileImageUrl
      )
    }
  }
}

package pmeet.pmeetserver.user.dto.response

import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender

data class UserResponseDto(
  val id: String,
  val provider: String?,
  val email: String,
  val name: String,
  val phoneNumber: String?,
  val gender: Gender?,
  val introductionComment: String?,
  val nickname: String,
  val isEmployed: Boolean,
  val profileImageUrl: String?
) {
  companion object {
    fun from(user: User): UserResponseDto {
      return UserResponseDto(
        id = user.id!!,
        provider = user.provider,
        email = user.email,
        name = user.name,
        phoneNumber = user.phoneNumber,
        gender = user.gender,
        introductionComment = user.introductionComment,
        nickname = user.nickname,
        isEmployed = user.isEmployed,
        profileImageUrl = user.profileImageUrl
      )
    }
  }
}

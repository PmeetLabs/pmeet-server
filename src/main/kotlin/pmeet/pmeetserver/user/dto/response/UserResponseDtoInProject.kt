package pmeet.pmeetserver.user.dto.response

import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender
import java.time.LocalDate

data class UserResponseDtoInProject(
  val id: String,
  val email: String,
  val phoneNumber: String?,
  val birthDate: LocalDate?,
  val gender: Gender?,
  val introductionComment: String?,
  val nickname: String,
  val profileImageUrl: String?
) {
  companion object {
    fun of(user: User, userProfileImageDownloadUrl: String?): UserResponseDtoInProject {
      return UserResponseDtoInProject(
        id = user.id!!,
        email = user.email,
        phoneNumber = user.phoneNumber,
        birthDate = user.birthDate,
        gender = user.gender,
        introductionComment = user.introductionComment,
        nickname = user.nickname,
        profileImageUrl = userProfileImageDownloadUrl
      )
    }
  }
}

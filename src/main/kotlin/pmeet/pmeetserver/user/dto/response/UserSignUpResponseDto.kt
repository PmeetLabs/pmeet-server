package pmeet.pmeetserver.user.dto.response

import pmeet.pmeetserver.user.domain.User

data class UserSignUpResponseDto(
  val id: String,
  val provider: String?,
  val email: String,
  val name: String,
  val nickname: String,
) {
  companion object {
    fun from(user: User): UserSignUpResponseDto {
      return UserSignUpResponseDto(
        id = user.id!!,
        provider = user.provider,
        email = user.email,
        name = user.name,
        nickname = user.nickname,
      )
    }
  }
}

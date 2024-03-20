package pmeet.pmeetserver.user.dto

import pmeet.pmeetserver.user.domain.User

data class UserResponseDto(
  val id: String,
  val provider: String?,
  val email: String,
  val name: String,
  val password: String?,
  val nickname: String
) {
  companion object {
    fun from(user: User): UserResponseDto {
      return UserResponseDto(
        id = user.id!!,
        provider = user.provider,
        email = user.email,
        name = user.name,
        password = user.password,
        nickname = user.nickname
      )
    }
  }
}

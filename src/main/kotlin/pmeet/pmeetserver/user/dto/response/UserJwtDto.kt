package pmeet.pmeetserver.user.dto.response


data class UserJwtDto(
  val userId: String,
  val accessToken: String,
  val refreshToken: String
) {
  companion object {
    fun of(
      userId: String,
      accessToken: String,
      refreshToken: String
    ): UserJwtDto {
      return UserJwtDto(
        userId = userId,
        accessToken = accessToken,
        refreshToken = refreshToken
      )
    }
  }
}

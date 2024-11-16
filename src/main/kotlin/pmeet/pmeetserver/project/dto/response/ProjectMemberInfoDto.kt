package pmeet.pmeetserver.project.dto.response

data class ProjectMemberInfoDto(
  val userId: String,
  val userName: String,
  val profileImageUrl: String?,
) {
  companion object {
    fun of(
      userId: String,
      userName: String,
      profileImageDownloadUrl: String?
    ): ProjectMemberInfoDto {
      return ProjectMemberInfoDto(
        userId = userId,
        userName = userName,
        profileImageUrl = profileImageDownloadUrl
      )
    }
  }
}

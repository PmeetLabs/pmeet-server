package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectBookmark
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.response.UserResponseDtoInProject
import java.time.LocalDateTime


data class ProjectWithUserResponseDto(
  val id: String,
  val userId: String,
  val title: String,
  val startDate: LocalDateTime,
  val endDate: LocalDateTime,
  val thumbNailUrl: String?,
  val techStacks: List<String>,
  val recruitments: List<RecruitmentResponseDto>,
  val description: String,
  val isCompleted: Boolean,
  val bookmarks: List<ProjectBookmark>,
  val isMyBookmark: Boolean,
  val userInfo: UserResponseDtoInProject,
  val createdAt: LocalDateTime
) {
  companion object {
    fun from(
      project: Project,
      user: User,
      requestedUserId: String,
      thumbNailDownloadUrl: String?,
      userProfileImageDownloadUrl: String?
    ): ProjectWithUserResponseDto {
      return ProjectWithUserResponseDto(
        id = project.id!!,
        userId = project.userId,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = thumbNailDownloadUrl,
        techStacks = project.techStacks!!,
        recruitments = project.recruitments.map {
          RecruitmentResponseDto(
            it.jobName,
            it.numberOfRecruitment
          )
        },
        description = project.description,
        isCompleted = project.isCompleted,
        bookmarks = project.bookmarkers,
        isMyBookmark = project.bookmarkers.any { it.userId == requestedUserId },
        userInfo = UserResponseDtoInProject.of(user, userProfileImageDownloadUrl),
        createdAt = project.createdAt
      )
    }
  }
}

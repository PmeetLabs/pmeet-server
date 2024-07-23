package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
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
  val description: String,
  val isCompleted: Boolean,
  val bookMarkers: List<String>,
  val userInfo: UserResponseDtoInProject,
  val createdAt: LocalDateTime
) {
  companion object {
    fun from(project: Project, user: User): ProjectWithUserResponseDto {
      return ProjectWithUserResponseDto(
        id = project.id!!,
        userId = project.userId,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = project.thumbNailUrl,
        techStacks = project.techStacks!!,
        description = project.description,
        isCompleted = project.isCompleted,
        bookMarkers = project.bookMarkers,
        userInfo = UserResponseDtoInProject.from(user),
        createdAt = project.createdAt
      )
    }
  }
}

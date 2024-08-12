package pmeet.pmeetserver.project.dto.request.tryout

import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import java.time.LocalDateTime

data class ProjectTryoutResponseDto(
  val id: String,
  val resumeId: String,
  val userId: String,
  val userName: String,
  val userSelfDescription: String,
  val positionName: String,
  val tryoutStatus: ProjectTryoutStatus,
  val projectId: String,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(projectTryout: ProjectTryout): ProjectTryoutResponseDto {
      return ProjectTryoutResponseDto(
        id = projectTryout.id!!,
        resumeId = projectTryout.resumeId,
        userId = projectTryout.userId,
        userName = projectTryout.userName,
        userSelfDescription = projectTryout.userSelfDescription,
        positionName = projectTryout.positionName,
        tryoutStatus = projectTryout.tryoutStatus,
        projectId = projectTryout.projectId,
        createdAt = projectTryout.createdAt
      )
    }
  }
}

package pmeet.pmeetserver.project.dto.request.tryout

import pmeet.pmeetserver.project.domain.ProjectTryout
import java.time.LocalDateTime

data class ProjectTryoutResponseDto(
  val id: String,
  val resumeId: String,
  val userId: String,
  val projectId: String,
  val createdAt: LocalDateTime
) {
  companion object {
    fun from(projectTryout: ProjectTryout): ProjectTryoutResponseDto {
      return ProjectTryoutResponseDto(
        id = projectTryout.id!!,
        resumeId = projectTryout.resumeId,
        userId = projectTryout.userId,
        projectId = projectTryout.projectId,
        createdAt = projectTryout.createdAt
      )
    }
  }
}

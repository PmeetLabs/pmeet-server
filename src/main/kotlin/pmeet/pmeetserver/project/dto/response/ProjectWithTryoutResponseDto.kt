package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
import java.time.LocalDateTime

data class ProjectWithTryoutResponseDto(
  val id: String,
  val title: String,
  val startDate: LocalDateTime,
  val endDate: LocalDateTime,
  val thumbNailUrl: String?,
  val techStacks: List<String>,
  val description: String,
  val completedAttachmentList: List<String>,
  val isCompleted: Boolean,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(
      project: Project, thumbNailDownloadUrl: String?,
    ): ProjectWithTryoutResponseDto {
      return ProjectWithTryoutResponseDto(
        id = project.id!!,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = thumbNailDownloadUrl,
        techStacks = project.techStacks!!,
        description = project.description,
        completedAttachmentList = project.completeAttachments,
        isCompleted = project.isCompleted,
        createdAt = project.createdAt,
      )
    }
  }
}

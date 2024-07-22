package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
import java.time.LocalDateTime

data class SearchProjectResponseDto(
  val id: String,
  val userId: String,
  val title: String,
  val startDate: LocalDateTime,
  val endDate: LocalDateTime,
  val thumbNailUrl: String?,
  val techStacks: List<String>,
  val jobNames: List<String>,
  val description: String,
  val isCompleted: Boolean,
  val bookMarked: Boolean,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
) {
  companion object {
    fun of(project: Project, userId: String): SearchProjectResponseDto {
      return SearchProjectResponseDto(
        id = project.id!!,
        userId = project.userId,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = project.thumbNailUrl,
        techStacks = project.techStacks!!,
        jobNames = project.recruitments.map { it.jobName },
        description = project.description,
        isCompleted = project.isCompleted,
        bookMarked = project.bookMarkers.contains(userId),
        createdAt = project.createdAt,
        updatedAt = project.updatedAt
      )
    }
  }
}

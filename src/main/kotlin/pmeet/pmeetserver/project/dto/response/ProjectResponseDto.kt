package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
import java.time.LocalDateTime

data class RecruitmentResponseDto(
  val jobName: String,
  var numberOfRecruitment: Int,
) {
}

data class ProjectResponseDto(
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
  val bookMarkers: List<String>,
  val createdAt: LocalDateTime
) {
  companion object {
    fun from(project: Project): ProjectResponseDto {
      return ProjectResponseDto(
        id = project.id!!,
        userId = project.userId,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = project.thumbNailUrl,
        techStacks = project.techStacks!!,
        recruitments = project.recruitments.map { RecruitmentResponseDto(it.jobName, it.numberOfRecruitment) },
        description = project.description,
        isCompleted = project.isCompleted,
        bookMarkers = project.bookMarkers,
        createdAt = project.createdAt
      )
    }
  }
}

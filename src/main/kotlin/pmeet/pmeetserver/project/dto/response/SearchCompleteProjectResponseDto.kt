package pmeet.pmeetserver.project.dto.response

import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectMember
import java.time.LocalDateTime

data class SearchCompleteProjectResponseDto(
  val id: String,
  val title: String,
  val startDate: LocalDateTime,
  val endDate: LocalDateTime,
  val thumbNailUrl: String?,
  val techStacks: List<String>,
  val description: String,
  val bookmarked: Boolean,
  val projectMembers: List<CompleteProjectMemberDto>,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime
) {
  companion object {
    fun of(
      project: Project,
      userId: String,
      projectMemberList: List<ProjectMember>,
      memberThumbnailMap: Map<String, String>,
      thumbNailDownloadUrl: String?
    ): SearchCompleteProjectResponseDto {
      return SearchCompleteProjectResponseDto(
        id = project.id!!,
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = thumbNailDownloadUrl,
        techStacks = project.techStacks!!,
        description = project.description,
        bookmarked = project.bookmarkers.any { it.userId == userId },
        projectMembers = projectMemberList.map { CompleteProjectMemberDto.of(it, memberThumbnailMap[it.id]) },
        createdAt = project.createdAt,
        updatedAt = project.updatedAt
      )
    }
  }
}

data class CompleteProjectMemberDto(
  val name: String,
  val profileImageUrl: String,
) {
  companion object {
    fun of(projectMember: ProjectMember, thumbNailUrl: String?): CompleteProjectMemberDto {
      return CompleteProjectMemberDto(
        name = projectMember.userName,
        profileImageUrl = thumbNailUrl.let { projectMember.userThumbnail.let { "" } }
      )
    }
  }
}

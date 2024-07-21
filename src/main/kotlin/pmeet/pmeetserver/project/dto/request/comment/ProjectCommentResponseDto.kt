package pmeet.pmeetserver.project.dto.request.comment

import java.time.LocalDateTime
import pmeet.pmeetserver.project.domain.ProjectComment

data class ProjectCommentResponseDto(
  val id: String,
  val parentCommentId: String?,
  val projectId: String,
  val userId: String,
  val content: String,
  val likerIdList: List<String>,
  val createdAt: LocalDateTime,
  val isDeleted: Boolean
) {
  companion object {
    fun from(comment: ProjectComment): ProjectCommentResponseDto {
      return ProjectCommentResponseDto(
        id = comment.id!!,
        parentCommentId = comment.parentCommentId,
        projectId = comment.projectId,
        userId = comment.userId,
        content = comment.content,
        likerIdList = comment.likerIdList,
        createdAt = comment.createdAt,
        isDeleted = comment.isDeleted
      )
    }
  }
}

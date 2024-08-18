package pmeet.pmeetserver.project.dto.comment.response

import java.time.LocalDateTime
import pmeet.pmeetserver.user.domain.User

data class GetProjectCommentResponseDto(
  val id: String,
  val parentCommentId: String?,
  val projectId: String,
  val userId: String,
  val userNickname: String,
  val userProfileImageUrl: String?,
  val content: String,
  val likerIdList: List<String>,
  val createdAt: LocalDateTime,
  val isDeleted: Boolean
) {
  companion object {
    fun from(comment: ProjectCommentResponseDto, user: User, url: String?): GetProjectCommentResponseDto {
      return GetProjectCommentResponseDto(
        id = comment.id,
        parentCommentId = comment.parentCommentId,
        projectId = comment.projectId,
        userId = comment.userId,
        userNickname = user.nickname,
        userProfileImageUrl = url,
        content = comment.content,
        likerIdList = comment.likerIdList,
        createdAt = comment.createdAt,
        isDeleted = comment.isDeleted
      )
    }
  }
}

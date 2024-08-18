package pmeet.pmeetserver.project.dto.comment.response

import java.time.LocalDateTime
import pmeet.pmeetserver.project.dto.comment.ProjectCommentWithChildDto
import pmeet.pmeetserver.user.domain.User

data class GetProjectCommentWithChildResponseDto(
  val id: String,
  val parentCommentId: String?,
  val projectId: String,
  val userId: String,
  val userNickname: String,
  val userProfileImageUrl: String?,
  val content: String,
  val likerIdList: List<String>,
  val createdAt: LocalDateTime,
  val isDeleted: Boolean,
  val childComments: List<GetProjectCommentResponseDto>
) {
  companion object {
    fun from(
      comment: ProjectCommentWithChildDto,
      user: User,
      url: String?,
      childList: List<GetProjectCommentResponseDto>
    ): GetProjectCommentWithChildResponseDto {
      return GetProjectCommentWithChildResponseDto(
        id = comment.id,
        parentCommentId = comment.parentCommentId,
        projectId = comment.projectId,
        userId = comment.userId,
        userNickname = user.nickname,
        userProfileImageUrl = url,
        content = comment.content,
        likerIdList = comment.likerIdList,
        createdAt = comment.createdAt,
        isDeleted = comment.isDeleted,
        childComments = childList
      )
    }
  }
}

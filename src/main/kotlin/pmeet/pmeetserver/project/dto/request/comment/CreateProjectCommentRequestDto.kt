package pmeet.pmeetserver.project.dto.request.comment

import jakarta.validation.constraints.Size

data class CreateProjectCommentRequestDto(
  val projectId: String,
  val parentCommentId: String? = null, // 대댓글일 경우 부모 댓글 ID
  @field:Size(min = 1, max = 100, message = "댓글은 1자부터 100자까지 입력 가능합니다.")
  var content: String
)

package pmeet.pmeetserver.project.dto.request.comment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateProjectCommentRequestDto(
  @field:NotBlank(message = "프로젝트 id는 필수입니다.")
  val projectId: String,
  val parentCommentId: String? = null, // 대댓글일 경우 부모 댓글 ID
  @field:NotBlank(message = "댓글 내용은 필수입니다.")
  @field:Size(min = 1, max = 100, message = "댓글은 1자부터 100자까지 입력 가능합니다.")
  var content: String
)

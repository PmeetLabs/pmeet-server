package pmeet.pmeetserver.project.dto.request.comment

import jakarta.validation.constraints.NotBlank

data class GetProjectCommentRequestDto (
  @field:NotBlank(message = "프로젝트 id는 필수입니다.")
  val projectId: String,
)


package pmeet.pmeetserver.project.dto.request.tryout

import jakarta.validation.constraints.NotBlank

data class CreateProjectTryoutRequestDto(
  @field:NotBlank(message = "이력서 id는 필수입니다.") val resumeId: String,
  @field:NotBlank(message = "프로젝트 id는 필수입니다.") val projectId: String
)
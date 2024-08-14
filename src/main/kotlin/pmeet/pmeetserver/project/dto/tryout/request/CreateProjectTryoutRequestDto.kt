package pmeet.pmeetserver.project.dto.tryout.request

import jakarta.validation.constraints.NotBlank

data class CreateProjectTryoutRequestDto(
  @field:NotBlank(message = "이력서 id는 필수입니다.") val resumeId: String,
  @field:NotBlank(message = "프로젝트 id는 필수입니다.") val projectId: String,
  @field:NotBlank(message = "지원 직무명은 필수입니다.") val positionName: String,
)

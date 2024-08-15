package pmeet.pmeetserver.project.dto.tryout.request

import jakarta.validation.constraints.NotBlank

data class PatchProjectTryoutRequestDto(
    @field:NotBlank(message = "프로젝트 id는 필수입니다.") val projectId: String,
    @field:NotBlank(message = "지원서 id는 필수입니다.") val tryoutId: String,
)

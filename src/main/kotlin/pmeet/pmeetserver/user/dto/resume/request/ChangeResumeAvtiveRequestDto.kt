package pmeet.pmeetserver.user.dto.resume.request

import jakarta.validation.constraints.NotBlank

data class ChangeResumeActiveRequestDto(
  @field:NotBlank(message = "이력서 id 는 필수입니다.") val id: String,
  val targetActiveStatus: Boolean
)

package pmeet.pmeetserver.user.dto.resume.request

import jakarta.validation.constraints.NotBlank

data class DeleteResumeRequestDto(
  @field:NotBlank(message = "이력서 id 는 필수입니다.") val id: String,
  @field:NotBlank(message = "이력서 생성자 id 는 필수입니다.") val userId: String) {}
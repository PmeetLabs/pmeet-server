package pmeet.pmeetserver.user.dto.techStack.request

import jakarta.validation.constraints.NotBlank

data class CreateTechStackRequestDto(
  @field:NotBlank(message = "기술 스택을 입력해 주세요.")
  val name: String
)

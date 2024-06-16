package pmeet.pmeetserver.user.dto.techStack.response

import pmeet.pmeetserver.user.domain.techStack.TechStack


data class TechStackResponseDto(
  val id: String,
  val name: String
) {
  companion object {
    fun from(techStack: TechStack): TechStackResponseDto {
      return TechStackResponseDto(
        id = techStack.id!!,
        name = techStack.name
      )
    }
  }
}

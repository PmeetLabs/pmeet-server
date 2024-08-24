package pmeet.pmeetserver.user.dto.resume.response

import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import java.time.LocalDateTime

data class SearchedResumeResponseDto(
  val id: String,
  val title: String,
  val selfDescription: String,
  val userName: String,
  val techStacks: List<TechStackResponseDto>,
  val userProfileImageUrl: String?,
  val updatedAt: LocalDateTime,
) {
  companion object {
    fun of(resume: Resume, profileImageDownloadUrl: String?): SearchedResumeResponseDto {
      return SearchedResumeResponseDto(
        id = resume.id!!,
        title = resume.title,
        selfDescription = resume.selfDescription ?: "",
        userName = resume.userName,
        techStacks = resume.techStacks.map { TechStackResponseDto.from(it) },
        userProfileImageUrl = profileImageDownloadUrl,
        updatedAt = resume.updatedAt
      )
    }
  }
}

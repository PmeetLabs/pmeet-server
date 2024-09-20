package pmeet.pmeetserver.user.dto.resume.response

import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import java.time.LocalDate
import java.time.LocalDateTime

data class ResumeJobExperienceResponseDto(
  val companyName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
) {
  companion object {
    fun from(jobExperience: JobExperience): ResumeJobExperienceResponseDto {
      return ResumeJobExperienceResponseDto(
        companyName = jobExperience.companyName,
        experiencePeriod = jobExperience.experiencePeriod,
        responsibilities = jobExperience.responsibilities
      )
    }
  }
}

data class ResumeProjectExperienceResponseDto(
  val projectName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
) {
  companion object {
    fun from(projectExperience: ProjectExperience): ResumeProjectExperienceResponseDto {
      return ResumeProjectExperienceResponseDto(
        projectName = projectExperience.projectName,
        experiencePeriod = projectExperience.experiencePeriod,
        responsibilities = projectExperience.responsibilities
      )
    }
  }
}

data class ResumeResponseDto(
  val id: String,
  val title: String,
  val isActive: Boolean,
  val userId: String,
  val userName: String,
  val userGender: Gender,
  val userBirthDate: LocalDate,
  val userPhoneNumber: String,
  val userEmail: String,
  val userProfileImageUrl: String?,
  val desiredJobs: List<JobResponseDto>,
  val techStacks: List<TechStackResponseDto>,
  val jobExperiences: List<ResumeJobExperienceResponseDto>,
  val projectExperiences: List<ResumeProjectExperienceResponseDto>,
  val portfolioFileUrl: String?,
  val portfolioUrl: List<String>,
  val selfDescription: String,
  val updatedAt: LocalDateTime,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun of(
      resume: Resume,
      profileImageDownloadUrl: String?,
      portfolioFileDownloadUrl: String? = null
    ): ResumeResponseDto {
      return ResumeResponseDto(
        id = resume.id!!,
        title = resume.title,
        isActive = resume.isActive,
        userId = resume.userId,
        userName = resume.userName,
        userGender = resume.userGender,
        userBirthDate = resume.userBirthDate,
        userPhoneNumber = resume.userPhoneNumber,
        userEmail = resume.userEmail,
        userProfileImageUrl = profileImageDownloadUrl,
        desiredJobs = resume.desiredJobs.map { JobResponseDto.from(it) },
        techStacks = resume.techStacks.map { TechStackResponseDto.from(it) },
        jobExperiences = resume.jobExperiences.map { ResumeJobExperienceResponseDto.from(it) },
        projectExperiences = resume.projectExperiences.map { ResumeProjectExperienceResponseDto.from(it) },
        portfolioFileUrl = portfolioFileDownloadUrl,
        portfolioUrl = resume.portfolioUrl,
        selfDescription = resume.selfDescription ?: "",
        updatedAt = resume.updatedAt,
        createdAt = resume.createdAt
      )
    }
  }
}

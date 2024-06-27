package pmeet.pmeetserver.user.dto.resume.request

import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.techStack.TechStack

data class ResumeJobRequestDto(
  val id: String,
  val name: String
) {
  fun toEntity(): Job {
    return Job(
      id = this.id,
      name = this.name
    )
  }
}

data class ResumeTechStackRequestDto(
  val id: String,
  val name: String
) {
  fun toEntity(): TechStack {
    return TechStack(
      id = this.id,
      name = this.name
    )
  }
}

data class ResumeJobExperienceRequestDto(
  val companyName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
) {
  fun toEntity(): JobExperience {
    return JobExperience(
      companyName = this.companyName,
      experiencePeriod = this.experiencePeriod,
      responsibilities = this.responsibilities
    )
  }
}

data class ResumeProjectExperienceRequestDto(
  val projectName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
) {
  fun toEntity(): ProjectExperience {
    return ProjectExperience(
      projectName = this.projectName,
      experiencePeriod = this.experiencePeriod,
      responsibilities = this.responsibilities
    )
  }
}
package pmeet.pmeetserver.user.domain.resume

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.techStack.TechStack
import java.time.LocalDate

data class JobExperience(
  val companyName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
)

data class ProjectExperience(
  val projectName: String,
  val experiencePeriod: ExperienceYear,
  val responsibilities: String
)

@Document
class Resume(
  @Id
  val id: String? = null, // mongodb auto id generation
  var title: String,
  var isActive: Boolean = false,
  val userId: String,
  val userName: String,
  val userGender: Gender,
  val userBirthDate: LocalDate,
  val userPhoneNumber: String,
  val userEmail: String,
  var userProfileImageUrl: String?,
  var desiredJobs: List<Job>,
  var techStacks: List<TechStack>,
  var jobExperiences: List<JobExperience>,
  var projectExperiences: List<ProjectExperience>,
  var portfolioFileUrl: String?,
  var portfolioUrl: List<String>,
  var selfDescription: String?
) {
  fun update(title: String? = null, userProfileImageUrl: String? = null,
             desiredJobs: List<Job>? = emptyList(),
             techStacks: List<TechStack>? = emptyList(),
             jobExperiences: List<JobExperience>? = emptyList(),
             projectExperiences: List<ProjectExperience>? = emptyList(),
             portfolioFileUrl: String? = null,
             portfolioUrl: List<String>? = emptyList(),
             selfDescription: String? = null
  ): Resume {
    if (title != null) this.title = title
    if (userProfileImageUrl != null) this.userProfileImageUrl = userProfileImageUrl
    if (desiredJobs != null) this.desiredJobs = desiredJobs
    if (techStacks != null) this.techStacks = techStacks
    if (jobExperiences != null) this.jobExperiences = jobExperiences
    if (projectExperiences != null) this.projectExperiences = projectExperiences
    if (portfolioFileUrl != null) this.portfolioFileUrl = portfolioFileUrl
    if (portfolioUrl != null) this.portfolioUrl = portfolioUrl
    if (selfDescription != null) this.selfDescription = selfDescription
    return this;
  }
}
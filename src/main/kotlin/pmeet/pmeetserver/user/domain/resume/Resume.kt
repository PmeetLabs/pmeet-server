package pmeet.pmeetserver.user.domain.resume

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
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
  val title: String,
  val isActive: Boolean = false,
  val userId: String,
  val userName: String,
  val userGender: Gender,
  val userBirthDate: LocalDate,
  val userPhoneNumber: String,
  val userEmail: String,
  val userProfileImageUrl: String?,
  val desiredJobs: List<Job>,
  val techStacks: List<TechStack>,
  val jobExperiences: List<JobExperience>,
  val projectExperiences: List<ProjectExperience>,
  val portfolioFileUrl: String?,
  val portfolioUrl: List<String>,
  val selfDescription: String?
  ) {
}
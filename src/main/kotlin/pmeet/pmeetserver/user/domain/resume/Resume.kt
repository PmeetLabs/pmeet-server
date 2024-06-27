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
  fun update(updatedResume: Resume): Resume {
    return Resume(
      id = this.id,
      title = updatedResume.title,
      isActive = updatedResume.isActive,
      userId = this.userId,// 수정 불가 항목
      userName = this.userName,// 수정 불가 항목
      userGender = this.userGender,// 수정 불가 항목
      userBirthDate = this.userBirthDate,// 수정 불가 항목
      userPhoneNumber = this.userPhoneNumber,// 수정 불가 항목
      userEmail = this.userEmail,// 수정 불가 항목
      userProfileImageUrl = updatedResume.userProfileImageUrl,
      desiredJobs = updatedResume.desiredJobs,
      techStacks = updatedResume.techStacks,
      jobExperiences = updatedResume.jobExperiences,
      projectExperiences = updatedResume.projectExperiences,
      portfolioFileUrl = updatedResume.portfolioFileUrl,
      portfolioUrl = updatedResume.portfolioUrl,
      selfDescription = updatedResume.selfDescription
    )
  }
}
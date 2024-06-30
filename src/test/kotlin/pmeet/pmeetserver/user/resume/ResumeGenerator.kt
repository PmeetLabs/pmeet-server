package pmeet.pmeetserver.user.resume

import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeJobExperienceRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeJobRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeProjectExperienceRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeTechStackRequestDto
import pmeet.pmeetserver.user.dto.resume.request.UpdateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeJobExperienceResponseDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeProjectExperienceResponseDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import java.time.LocalDate

object ResumeGenerator {

  internal fun createMockCreateResumeRequestDto(): CreateResumeRequestDto {
    return CreateResumeRequestDto(
      title = "Software Engineer",
      isActive = false,
      userId = "John-id",
      userName = "John Doe",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(1990, 1, 1),
      userPhoneNumber = "010-1234-5678",
      userEmail = "john.doe@example.com",
      userProfileImageUrl = "http://example.com/profile.jpg",
      desiredJobs = listOf(
        ResumeJobRequestDto(
          id = "job1",
          name = "Backend Developer"
        ),
        ResumeJobRequestDto(
          id = "job2",
          name = "Frontend Developer"
        )
      ),
      techStacks = listOf(
        ResumeTechStackRequestDto(
          id = "tech1",
          name = "Kotlin"
        ),
        ResumeTechStackRequestDto(
          id = "tech2",
          name = "React"
        )
      ),
      jobExperiences = listOf(
        ResumeJobExperienceRequestDto(
          companyName = "ABC Corp",
          experiencePeriod = ExperienceYear.YEAR_03,
          responsibilities = "Developed backend services"
        ),
        ResumeJobExperienceRequestDto(
          companyName = "XYZ Inc",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Worked on frontend development"
        )
      ),
      projectExperiences = listOf(
        ResumeProjectExperienceRequestDto(
          projectName = "Project A",
          experiencePeriod = ExperienceYear.YEAR_01,
          responsibilities = "Led the project development"
        ),
        ResumeProjectExperienceRequestDto(
          projectName = "Project B",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Contributed to backend services"
        )
      ),
      portfolioFileUrl = "http://example.com/portfolio.pdf",
      portfolioUrl = listOf("http://example.com/project1", "http://example.com/project2"),
      selfDescription = "Passionate software engineer with a focus on backend development."
    )
  }

  internal fun createMockResumeResponseDto(): ResumeResponseDto {
    return ResumeResponseDto(
      id = "resume-id",
      title = "Software Engineer",
      isActive = false,
      userId = "John-id",
      userName = "John Doe",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(1990, 1, 1),
      userPhoneNumber = "010-1234-5678",
      userEmail = "john.doe@example.com",
      userProfileImageUrl = "http://example.com/profile.jpg",
      desiredJobs = listOf(
        JobResponseDto(
          id = "job1",
          name = "Backend Developer"
        ),
        JobResponseDto(
          id = "job2",
          name = "Frontend Developer"
        )
      ),
      techStacks = listOf(
        TechStackResponseDto(
          id = "tech1",
          name = "Kotlin"
        ),
        TechStackResponseDto(
          id = "tech2",
          name = "React"
        )
      ),
      jobExperiences = listOf(
        ResumeJobExperienceResponseDto(
          companyName = "ABC Corp",
          experiencePeriod = ExperienceYear.YEAR_03,
          responsibilities = "Developed backend services"
        ),
        ResumeJobExperienceResponseDto(
          companyName = "XYZ Inc",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Worked on frontend development"
        )
      ),
      projectExperiences = listOf(
        ResumeProjectExperienceResponseDto(
          projectName = "Project A",
          experiencePeriod = ExperienceYear.YEAR_01,
          responsibilities = "Led the project development"
        ),
        ResumeProjectExperienceResponseDto(
          projectName = "Project B",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Contributed to backend services"
        )
      ),
      portfolioFileUrl = "http://example.com/portfolio.pdf",
      portfolioUrl = listOf("http://example.com/project1", "http://example.com/project2"),
      selfDescription = "Passionate software engineer with a focus on backend development."
    )
  }

  internal fun generateResume(): Resume {
    return Resume(
      id = "resume-id",
      title = "Software Engineer",
      userId = "John-id",
      userName = "John Doe",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(1990, 1, 1),
      userPhoneNumber = "010-1234-5678",
      userEmail = "john.doe@example.com",
      userProfileImageUrl = "http://example.com/profile.jpg",
      desiredJobs = listOf(
        Job(
          id = "job1",
          name = "Backend Developer"
        ),
        Job(
          id = "job2",
          name = "Frontend Developer"
        )
      ),
      techStacks = listOf(
        TechStack(
          id = "tech1",
          name = "Kotlin"
        ),
        TechStack(
          id = "tech2",
          name = "React"
        )
      ),
      jobExperiences = listOf(
        JobExperience(
          companyName = "ABC Corp",
          experiencePeriod = ExperienceYear.YEAR_03,
          responsibilities = "Developed backend services"
        ),
        JobExperience(
          companyName = "XYZ Inc",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Worked on frontend development"
        )
      ),
      projectExperiences = listOf(
        ProjectExperience(
          projectName = "Project A",
          experiencePeriod = ExperienceYear.YEAR_01,
          responsibilities = "Led the project development"
        ),
        ProjectExperience(
          projectName = "Project B",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Contributed to backend services"
        )
      ),
      portfolioFileUrl = "http://example.com/portfolio.pdf",
      portfolioUrl = listOf("http://example.com/project1", "http://example.com/project2"),
      selfDescription = "Passionate software engineer with a focus on backend development."
    )
  }

  internal fun createMockUpdateResumeRequestDto(): UpdateResumeRequestDto {
    return UpdateResumeRequestDto(
      id = "resume-id",
      title = "Engineer",
      isActive = false,
      userId = "John-id",
      userName = "John Doe",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(1990, 1, 1),
      userPhoneNumber = "010-1234-5678",
      userEmail = "john.doe@example.com",
      userProfileImageUrl = "http://example.com/profile.jpg",
      desiredJobs = listOf(
        ResumeJobRequestDto(
          id = "job1",
          name = "Backend Developer"
        ),
        ResumeJobRequestDto(
          id = "job2",
          name = "Frontend Developer"
        )
      ),
      techStacks = listOf(
        ResumeTechStackRequestDto(
          id = "tech1",
          name = "Kotlin"
        ),
        ResumeTechStackRequestDto(
          id = "tech2",
          name = "React"
        )
      ),
      jobExperiences = listOf(
        ResumeJobExperienceRequestDto(
          companyName = "ABC Corp",
          experiencePeriod = ExperienceYear.YEAR_03,
          responsibilities = "Developed backend services"
        ),
        ResumeJobExperienceRequestDto(
          companyName = "XYZ Inc",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Worked on frontend development"
        )
      ),
      projectExperiences = listOf(
        ResumeProjectExperienceRequestDto(
          projectName = "Project A",
          experiencePeriod = ExperienceYear.YEAR_01,
          responsibilities = "Led the project development"
        ),
        ResumeProjectExperienceRequestDto(
          projectName = "Project B",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Contributed to backend services"
        )
      ),
      portfolioFileUrl = "http://example.com/portfolio.pdf",
      portfolioUrl = listOf("http://example.com/project1", "http://example.com/project2"),
      selfDescription = "Passionate software engineer with a focus on backend development."
    )
  }

  internal fun createMockDeleteResumeRequestDto(): DeleteResumeRequestDto {
    return DeleteResumeRequestDto(
      id = "resume-id",
      userId = "John-id",
    )
  }
}
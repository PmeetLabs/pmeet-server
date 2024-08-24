package pmeet.pmeetserver.user.resume

import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.dto.resume.request.ChangeResumeActiveRequestDto
import pmeet.pmeetserver.user.dto.resume.request.CopyResumeRequestDto
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
import java.time.LocalDateTime
import java.time.Month

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
      selfDescription = "Passionate software engineer with a focus on backend development.",
      updatedAt = LocalDateTime.now(),
      createdAt = LocalDateTime.now()
    )
  }

  internal fun createMockResumeResponseDtoList(): List<ResumeResponseDto> {
    val resumes = generateResumeList()
    return resumes.map { ResumeResponseDto.of(it, "profileImageDownloadUrl", "portfolioFileDownloadUrl") }
  }

  internal fun createMockResumeCopyResponseDto(): ResumeResponseDto {
    return ResumeResponseDto(
      id = "resume-id",
      title = "[복사] Software Engineer",
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
      selfDescription = "Passionate software engineer with a focus on backend development.",
      updatedAt = LocalDateTime.now(),
      createdAt = LocalDateTime.now()
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

  internal fun generateActiveResume(): Resume {
    return Resume(
      id = "resume-id-active",
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
      isActive = true,
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

  internal fun generateResumeByResumeIdUserId(resumeId: String, userId: String): Resume {
    return Resume(
      id = resumeId,
      title = "Software Engineer",
      userId = userId,
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

  internal fun generateResumeList(): List<Resume> {
    val resume1 = generateResumeByResumeIdUserId("resume1", "user2")
    val resume2 = generateResumeByResumeIdUserId("resume2", "user2")
    val resume3 = generateResumeByResumeIdUserId("resume3", "user2")
    return mutableListOf(resume1, resume2, resume3)
  }

  internal fun generateCopiedResume(): Resume {
    val originalResume = generateResume()
    val copiedResume = originalResume.copy()
    ReflectionTestUtils.setField(copiedResume, "id", "copied-resume-id")
    return copiedResume
  }

  internal fun createMockUpdateResumeRequestDto(): UpdateResumeRequestDto {
    return UpdateResumeRequestDto(
      id = "resume-id",
      title = "Engineer",
      isActive = false,
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

  internal fun generateUpdatedResume(): Resume {
    return Resume(
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

  internal fun createMockDeleteResumeRequestDto(): DeleteResumeRequestDto {
    return DeleteResumeRequestDto(
      id = "resume-id",
      userId = "John-id",
    )
  }

  internal fun createMockCopyResumeRequestDto(): CopyResumeRequestDto {
    return CopyResumeRequestDto(
      id = "resume-id"
    )
  }

  internal fun createMockChangeResumeActiveRequestDto(targetActive: Boolean): ChangeResumeActiveRequestDto {
    return ChangeResumeActiveRequestDto(
      id = "resume-id",
      targetActiveStatus = targetActive,
    )
  }

  internal fun createResumeWithTitleAndJobAndNickNameAndBookMark(
    title: String,
    job: String,
    nickName: String,
    bookmarkNumber: Int,
    id: String?
  ): Resume {
    val resume = Resume(
      title = title,
      userId = "John-id",
      userName = nickName,
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(1990, 1, 1),
      userPhoneNumber = "010-1234-5678",
      userEmail = "john.doe@example.com",
      userProfileImageUrl = "http://example.com/profile.jpg",
      desiredJobs = listOf(
        Job(
          name = "test_job1"
        ),
        Job(
          name = "test_job2"
        ),
        Job(
          name = job
        )
      ),
      isActive = true,
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
      selfDescription = "Passionate software engineer with a focus on backend development.",
      updatedAt = LocalDateTime.of(2022, Month.DECEMBER, 5, 0, 0, 0).minusDays(bookmarkNumber.toLong()),
      createdAt = LocalDateTime.of(2019, Month.DECEMBER, 5, 0, 0, 0),
    )
    if (id != null) {
      resume.id = id
    }

    for (i in 1 until bookmarkNumber) {
      resume.addBookmark(i.toString())
    }
    return resume
  }

  fun generateResumeListForSlice(): List<Resume> {
    return listOf(
      createResumeWithTitleAndJobAndNickNameAndBookMark("title1", "job1", "nickname1", 1, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title2", "job2", "nickname2", 2, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title3", "job3", "nickname3", 3, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title4", "job4", "nickname4", 4, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title5", "job5", "nickname5", 5, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title6", "job6", "nickname6", 6, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title7", "job7", "nickname7", 7, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title8", "job8", "nickname8", 8, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title9", "job9", "nickname9", 9, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title10", "job10", "nickname10", 10, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title11", "job11", "nickname11", 11, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title12", "job12", "nickname12", 12, null),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title13", "job13", "nickname13", 13, null),
    )
  }

  fun generateMockResumeListForSlice(): List<Resume> {
    return listOf(
      createResumeWithTitleAndJobAndNickNameAndBookMark("title1", "job1", "nickname1", 1, "resume-test-id1"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title2", "job2", "nickname2", 2, "resume-test-id2"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title3", "job3", "nickname3", 3, "resume-test-id3"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title4", "job4", "nickname4", 4, "resume-test-id4"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title5", "job5", "nickname5", 5, "resume-test-id5"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title6", "job6", "nickname6", 6, "resume-test-id6"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title7", "job7", "nickname7", 7, "resume-test-id7"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title8", "job8", "nickname8", 8, "resume-test-id8"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title9", "job9", "nickname9", 9, "resume-test-id9"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title10", "job10", "nickname10", 10, "resume-test-id10"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title11", "job11", "nickname11", 11, "resume-test-id11"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title12", "job12", "nickname12", 12, "resume-test-id12"),
      createResumeWithTitleAndJobAndNickNameAndBookMark("title13", "job13", "nickname13", 13, "resume-test-id13"),
    )
  }
}

package pmeet.pmeetserver.user.resume.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.BadRequestException
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator
import pmeet.pmeetserver.user.service.resume.ResumeService
import reactor.core.publisher.Mono
import java.time.LocalDate

@ExperimentalCoroutinesApi
internal class ResumeServiceUnitTest : DescribeSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val resumeRepository = mockk<ResumeRepository>(relaxed = true)

  lateinit var resumeService: ResumeService

  lateinit var job: Job
  lateinit var techStack: TechStack
  lateinit var jobExperience: JobExperience
  lateinit var projectExperience: ProjectExperience
  lateinit var resume: Resume

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    resumeService = ResumeService(resumeRepository)
    job = Job(
      name = "testName",
    )

    techStack = TechStack(
      name = "testName",
    )

    jobExperience = JobExperience(
      companyName = "companyName",
      experiencePeriod = ExperienceYear.YEAR_01,
      responsibilities = "jobExperienceResponsibility",
    )

    projectExperience = ProjectExperience(
      projectName = "projectName",
      experiencePeriod = ExperienceYear.YEAR_00,
      responsibilities = "projectExperienceResponsibility",
    )

    resume = Resume(
      id = "resume-id",
      title = "title",
      isActive = false,
      userId = "John-id",
      userName = "userName",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(2024, 6, 20),
      userPhoneNumber = "010-1234-5678",
      userEmail = "userEmail@example.com",
      userProfileImageUrl = "userProfileImage.com",
      desiredJobs = mutableListOf(job),
      techStacks = mutableListOf(techStack),
      jobExperiences = mutableListOf(jobExperience),
      projectExperiences = mutableListOf(projectExperience),
      portfolioFileUrl = "portfolioFile.com",
      portfolioUrl = mutableListOf("portfolioUrl.com"),
      selfDescription = "self description",
    )

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("이력서가 주어지면") {
      it("저장 후 이력서를 반환한다") {
        runTest {
          every { resumeRepository.countByUserId(any()) } answers { Mono.just(1) }
          every { resumeRepository.save(any()) } answers { Mono.just(resume) }
          every { resumeRepository.findById("resume-id") } answers { Mono.just(resume) }

          val result = resumeService.save(resume)

          result.title shouldBe resume.title
          result.isActive shouldBe resume.isActive
          result.userId shouldBe resume.userId
          result.userName shouldBe resume.userName
          result.userGender shouldBe resume.userGender
          result.userBirthDate shouldBe resume.userBirthDate
          result.userPhoneNumber shouldBe resume.userPhoneNumber
          result.userEmail shouldBe resume.userEmail
          result.userProfileImageUrl shouldBe resume.userProfileImageUrl
          result.desiredJobs.first.name shouldBe resume.desiredJobs.first.name
          result.techStacks.first.name shouldBe resume.techStacks.first.name
          result.jobExperiences.first.companyName shouldBe resume.jobExperiences.first.companyName
          result.projectExperiences.first.projectName shouldBe resume.projectExperiences.first.projectName
          result.portfolioFileUrl shouldBe resume.portfolioFileUrl
          result.portfolioUrl.first shouldBe resume.portfolioUrl.first
          result.selfDescription shouldBe resume.selfDescription
        }
      }
    }

    context("이력서가 5개 초과로 생성 요청이라면") {
      every { resumeRepository.countByUserId(any()) } answers { Mono.just(5) }
      it("BadRequestException을 던진다") {
        runTest {
          val exception = shouldThrow<BadRequestException> {
            resumeService.save(resume)
          }

          exception.errorCode shouldBe ErrorCode.RESUME_NUMBER_EXCEEDED
        }
      }
    }
  }

  describe("update") {
    context("이력서를 업데이트하는 경우") {
      it("저장 후 이력서를 반환한다") {
        runTest {
          every { resumeRepository.findById("resume-id") } answers { Mono.just(resume) }

          val resumeUpdateRequestDto = ResumeGenerator.createMockUpdateResumeRequestDto();

          val result = resumeService.update(resumeUpdateRequestDto.toEntity(), resumeUpdateRequestDto.id)

          result.title shouldBe resumeUpdateRequestDto.title
          result.isActive shouldBe resumeUpdateRequestDto.isActive
          result.userProfileImageUrl shouldBe resumeUpdateRequestDto.userProfileImageUrl
          result.desiredJobs.first.name shouldBe resumeUpdateRequestDto.desiredJobs.first.name
          result.techStacks.first.name shouldBe resumeUpdateRequestDto.techStacks.first.name
          result.jobExperiences.first.companyName shouldBe resumeUpdateRequestDto.jobExperiences.first.companyName
          result.projectExperiences.first.projectName shouldBe resumeUpdateRequestDto.projectExperiences.first.projectName
          result.portfolioFileUrl shouldBe resumeUpdateRequestDto.portfolioFileUrl
          result.portfolioUrl.first shouldBe resumeUpdateRequestDto.portfolioUrl.first
          result.selfDescription shouldBe resumeUpdateRequestDto.selfDescription
        }
      }


      it("존재하지 않는 ID로 업데이트 시도 시 NoSuchElementException 발생시킨다") {
        runTest {
          every { resumeRepository.findById("non-existent-id") } returns Mono.empty()

          val resumeUpdateRequestDto = ResumeGenerator.createMockUpdateResumeRequestDto()

          val exception = shouldThrow<BadRequestException> {
            resumeService.update(resumeUpdateRequestDto.toEntity(), "non-existent-id")
          }
          exception.errorCode shouldBe ErrorCode.RESUME_NOT_FOUND

        }
      }
    }
  }

})
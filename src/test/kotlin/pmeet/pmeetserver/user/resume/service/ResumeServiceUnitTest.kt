package pmeet.pmeetserver.user.resume.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.data.domain.PageRequest
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.BadRequestException
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockUpdateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateMockResumeListForSlice
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeList
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateUpdatedResume
import pmeet.pmeetserver.user.service.resume.ResumeService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

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
  lateinit var resumeList: List<Resume>

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

    resume = generateResume()

    resumeList = generateResumeList()


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
          val requestTime = LocalDateTime.now().minusMinutes(1L)

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
          result.desiredJobs.first().name shouldBe resume.desiredJobs.first().name
          result.techStacks.first().name shouldBe resume.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe resume.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe resume.projectExperiences.first().projectName
          result.portfolioFileUrls shouldBe resume.portfolioFileUrls
          result.portfolioUrl.first() shouldBe resume.portfolioUrl.first()
          result.selfDescription shouldBe resume.selfDescription
          result.createdAt shouldBeAfter requestTime
          result.updatedAt shouldBeAfter requestTime
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
          val resumeUpdateRequestDto = createMockUpdateResumeRequestDto();
          every { resumeRepository.save(any()) } answers { Mono.just(generateUpdatedResume()) }
          val result = resumeService.update(generateUpdatedResume())

          result.title shouldBe resumeUpdateRequestDto.title
          result.isActive shouldBe resumeUpdateRequestDto.isActive
          result.userProfileImageUrl shouldBe resumeUpdateRequestDto.userProfileImageUrl
          result.desiredJobs.first().name shouldBe resumeUpdateRequestDto.desiredJobs.first().name
          result.techStacks.first().name shouldBe resumeUpdateRequestDto.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe resumeUpdateRequestDto.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe resumeUpdateRequestDto.projectExperiences.first().projectName
          result.portfolioFileUrls shouldBe resumeUpdateRequestDto.portfolioFileUrls
          result.portfolioUrl.first() shouldBe resumeUpdateRequestDto.portfolioUrl.first()
          result.selfDescription shouldBe resumeUpdateRequestDto.selfDescription
        }
      }
    }
  }

  describe("delete") {
    context("이력서를 삭제하는 경우") {
      it("이력서를 삭제한다") {
        runTest {
          val resumeDeleteRequestDto = ResumeGenerator.createMockDeleteResumeRequestDto();
          every { resumeRepository.deleteById(resumeDeleteRequestDto.id) } answers { Mono.empty() }

          resumeService.delete(resume)

          coVerify { resume.id?.let { resumeRepository.deleteById(it) } }

        }
      }
    }
  }

  describe("change status") {
    context("이력서의 프미팅 게시 상태를 참으로 변경한다.") {
      it("이력서의 프미팅 게시 여부가 참으로 변경된다.") {
        runTest {
          val capturedResumeSlot = slot<Resume>()
          every { resumeRepository.save(capture(capturedResumeSlot)) } answers { Mono.just(capturedResumeSlot.captured) }

          resumeService.changeActive(resume, true)

          coVerify { resumeRepository.save(any()) }
          capturedResumeSlot.captured.isActive shouldBe true
        }
      }
    }

    context("이력서의 프미팅 게시 상태를 거짓으로 변경한다.") {
      it("이력서의 프미팅 게시 여부가 거짓으로 변경된다.") {
        runTest {
          val capturedResumeSlot = slot<Resume>()
          every { resumeRepository.save(capture(capturedResumeSlot)) } answers { Mono.just(capturedResumeSlot.captured) }

          resumeService.changeActive(resume, false)

          coVerify { resumeRepository.save(any()) }
          capturedResumeSlot.captured.isActive shouldBe false
        }
      }
    }
  }

  describe("getAllByUserId") {
    context("본인 소유의 이력서를 조회하는 경우") {
      it("이력서 목록을 조회한다") {
        runTest {
          every { resumeRepository.findAllByUserId("user2") } answers { Flux.fromIterable(resumeList) }

          val result = resumeService.getAllByUserId("user2")

          result.size shouldBe resumeList.size
        }
      }
    }
  }

  describe("searchSliceByFilter") {
    context("검색 조건이 주어지면") {
      it("Slice<Resume>를 반환한다") {
        val pageNumber = 0
        val pageSize = 10
        val resumeListForSlice = generateMockResumeListForSlice().subList(0, pageSize)

        runTest {
          every {
            resumeRepository.findAllByFilter(
              any(),
              any(),
              any(),
              any(),
              any()
            )
          } answers { Flux.fromIterable(resumeListForSlice) }

          val result = resumeService.searchSliceByFilter(
            "testUserId",
            ResumeFilterType.ALL,
            "",
            ResumeOrderType.RECENT,
            PageRequest.of(pageNumber, pageSize)
          )

          result.size shouldBe pageSize
          for (i in 0..resumeListForSlice.size - 1) {
            result.content.get(i).title shouldBe resumeListForSlice[i].title
          }
        }
      }
    }
  }

})

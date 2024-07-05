package pmeet.pmeetserver.user.resume.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCreateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockDeleteResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockUpdateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import pmeet.pmeetserver.user.service.resume.ResumeService

@ExperimentalCoroutinesApi
class ResumeFacadeServiceUnitTest : DescribeSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val resumeService = mockk<ResumeService>(relaxed = true)

  lateinit var resumeFacadeService: ResumeFacadeService

  lateinit var resume: Resume

  beforeSpec {
    Dispatchers.setMain(testDispatcher)

    resumeFacadeService = ResumeFacadeService(resumeService)

    resume = generateResume()
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("createResume") {
    context("이력서를 저장하는 경우") {
      it("저장 후 이력서를 반환한다.") {
        runTest {
          coEvery { resumeService.save(any()) } answers { resume }
          val resumeCreateRequest = createMockCreateResumeRequestDto()
          val result = resumeFacadeService.createResume(resumeCreateRequest)

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
          result.portfolioFileUrl shouldBe resume.portfolioFileUrl
          result.portfolioUrl.first() shouldBe resume.portfolioUrl.first()
          result.selfDescription shouldBe resume.selfDescription
        }
      }
    }
  }

  describe("updateResume") {
    context("이력서를 업데이트하는 경우") {
      it("저장 후 이력서를 반환한다") {
        runTest {
          val updateRequest = createMockUpdateResumeRequestDto()
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { resumeService.update(any()) } answers { updateRequest.toEntity() }

          val result = resumeFacadeService.updateResume(updateRequest.userId, updateRequest)
          result.title shouldBe updateRequest.title
          result.isActive shouldBe updateRequest.isActive
          result.desiredJobs.first().name shouldBe updateRequest.desiredJobs.first().name
          result.techStacks.first().name shouldBe updateRequest.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe updateRequest.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe updateRequest.projectExperiences.first().projectName
          result.portfolioFileUrl shouldBe updateRequest.portfolioFileUrl
          result.portfolioUrl.first() shouldBe updateRequest.portfolioUrl.first()
          result.selfDescription shouldBe updateRequest.selfDescription
        }
      }
      it("존재하지 않는 resume ID로 업데이트 시도 시 EntityNotFoundException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } throws EntityNotFoundException(ErrorCode.RESUME_NOT_FOUND)

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<EntityNotFoundException> {
            resumeFacadeService.updateResume(updateRequest.userId, updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_NOT_FOUND
        }
      }
      it("권한이 없는 userId 로 업데이트 시도 시 UnauthorizedException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<UnauthorizedException> {
            resumeFacadeService.updateResume("no-auth-user", updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_UPDATE_UNAUTHORIZED
        }
      }
    }
  }

  describe("deleteResume") {
    context("이력서를 삭제하는 경우") {
      it("이력서를 삭제한다.") {
        runTest {
          val deleteRequest = createMockDeleteResumeRequestDto()
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          resumeFacadeService.deleteResume(deleteRequest)

          coVerify { resumeService.delete(deleteRequest.id, deleteRequest.userId) }
        }
      }
      it("존재하지 않는 resume ID로 업데이트 시도 시 EntityNotFoundException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } throws EntityNotFoundException(ErrorCode.RESUME_NOT_FOUND)

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<EntityNotFoundException> {
            resumeFacadeService.updateResume(updateRequest.userId, updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_NOT_FOUND
        }
      }

      it("권한이 없는 userId 로 삭제 시도 시 UnauthorizedException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val deleteRequest = DeleteResumeRequestDto(
            id = "resume-id",
            userId = "John-id-wrong",
          )

          val exception = shouldThrow<UnauthorizedException> {
            resumeFacadeService.deleteResume(deleteRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_DELETE_UNAUTHORIZED
        }
      }
    }
  }
})

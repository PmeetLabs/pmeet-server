package pmeet.pmeetserver.user.resume.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.date.shouldBeAfter
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
import org.springframework.data.domain.PageRequest
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.common.utils.page.SliceResponse
import pmeet.pmeetserver.file.service.FileService
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockChangeResumeActiveRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCopyResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCreateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockDeleteResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockUpdateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateCopiedResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateMockResumeListForSlice
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeList
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateUpdatedResume
import pmeet.pmeetserver.user.service.UserService
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import pmeet.pmeetserver.user.service.resume.ResumeService
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class ResumeFacadeServiceUnitTest : DescribeSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val resumeService = mockk<ResumeService>(relaxed = true)
  val fileService = mockk<FileService>(relaxed = true)
  val userService = mockk<UserService>(relaxed = true)

  lateinit var resumeFacadeService: ResumeFacadeService

  lateinit var resume: Resume
  lateinit var resumeId: String
  lateinit var resumeList: List<Resume>
  lateinit var user: User
  lateinit var userId: String

  beforeSpec {
    Dispatchers.setMain(testDispatcher)

    resumeFacadeService = ResumeFacadeService(resumeService, fileService, userService)

    resume = generateResume()
    resumeId = resume.id!!
    resumeList = generateResumeList()

    userId = "testUserId"

    user = User(
      id = userId,
      email = "testEmail@test.com",
      name = "testName",
      nickname = "nickname",
      phoneNumber = "phone",
      gender = Gender.MALE,
      profileImageUrl = "image-url"
    )
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
          val profileImageDownloadUrl = "profile-image-download-url"
          val portfolioFileDownloadUrls = listOf("portfolio-file-download-url", "portfolio-file-download-url2")
          coEvery { fileService.generatePreSignedUrlToDownload(resume.userProfileImageUrl!!) } answers { profileImageDownloadUrl }
          coEvery { fileService.generatePreSignedUrlsToDownload(resume.portfolioFileUrls) } answers { portfolioFileDownloadUrls }

          val result = resumeFacadeService.createResume(resumeCreateRequest)

          result.title shouldBe resume.title
          result.isActive shouldBe resume.isActive
          result.userId shouldBe resume.userId
          result.userName shouldBe resume.userName
          result.userGender shouldBe resume.userGender
          result.userBirthDate shouldBe resume.userBirthDate
          result.userPhoneNumber shouldBe resume.userPhoneNumber
          result.userEmail shouldBe resume.userEmail
          result.userProfileImageUrl shouldBe profileImageDownloadUrl
          result.desiredJobs.first().name shouldBe resume.desiredJobs.first().name
          result.techStacks.first().name shouldBe resume.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe resume.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe resume.projectExperiences.first().projectName
          result.portfolioFileUrls shouldBe portfolioFileDownloadUrls
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
          val requestTime = LocalDateTime.now().minusMinutes(1L)
          val updateRequest = createMockUpdateResumeRequestDto()
          val profileImageDownloadUrl = "profile-image-download-url"
          val portfolioFileDownloadUrls = listOf("portfolio-file-download-url", "portfolio-file-download-url2")
          coEvery { fileService.generatePreSignedUrlToDownload(updateRequest.userProfileImageUrl!!) } answers { profileImageDownloadUrl }
          coEvery { fileService.generatePreSignedUrlsToDownload(updateRequest.portfolioFileUrls) } answers { portfolioFileDownloadUrls }
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { resumeService.update(any()) } answers { generateUpdatedResume() }

          val result = resumeFacadeService.updateResume(resume.userId, updateRequest)

          result.title shouldBe updateRequest.title
          result.isActive shouldBe updateRequest.isActive
          result.userProfileImageUrl shouldBe profileImageDownloadUrl
          result.desiredJobs.first().name shouldBe updateRequest.desiredJobs.first().name
          result.techStacks.first().name shouldBe updateRequest.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe updateRequest.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe updateRequest.projectExperiences.first().projectName
          result.portfolioFileUrls shouldBe portfolioFileDownloadUrls
          result.portfolioUrl.first() shouldBe updateRequest.portfolioUrl.first()
          result.selfDescription shouldBe updateRequest.selfDescription
          result.updatedAt shouldBeAfter requestTime
        }
      }

      it("resume 의 update 메서드는 null 인 필드는 제외하고 이력서를 업데이트한다.") {
        runTest {

          resume = resume.update(title = "resume-title")
          val originalResume = generateResume()

          resume.isActive shouldBe originalResume.isActive
          resume.desiredJobs.first().name shouldBe originalResume.desiredJobs.first().name
          resume.techStacks.first().name shouldBe originalResume.techStacks.first().name
          resume.jobExperiences.first().companyName shouldBe originalResume.jobExperiences.first().companyName
          resume.projectExperiences.first().projectName shouldBe originalResume.projectExperiences.first().projectName
          resume.portfolioFileUrls shouldBe originalResume.portfolioFileUrls
          resume.portfolioUrl.first() shouldBe originalResume.portfolioUrl.first()
          resume.selfDescription shouldBe originalResume.selfDescription
        }
      }

      it("존재하지 않는 resume ID로 업데이트 시도 시 EntityNotFoundException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } throws EntityNotFoundException(ErrorCode.RESUME_NOT_FOUND)

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<EntityNotFoundException> {
            resumeFacadeService.updateResume("mock-id", updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_NOT_FOUND
        }
      }
      it("권한이 없는 userId 로 업데이트 시도 시 ForbiddenRequestException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<ForbiddenRequestException> {
            resumeFacadeService.updateResume("no-auth-user", updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_UPDATE_FORBIDDEN
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

          coVerify { resumeService.delete(resume) }
        }
      }
      it("존재하지 않는 resume ID로 업데이트 시도 시 EntityNotFoundException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } throws EntityNotFoundException(ErrorCode.RESUME_NOT_FOUND)

          val updateRequest = createMockUpdateResumeRequestDto()

          val exception = shouldThrow<EntityNotFoundException> {
            resumeFacadeService.updateResume(generateUpdatedResume().userId, updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_NOT_FOUND
        }
      }

      it("권한이 없는 userId 로 삭제 시도 시 ForbiddenRequestException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val deleteRequest = DeleteResumeRequestDto(
            id = "resume-id",
            userId = "John-id-wrong",
          )

          val exception = shouldThrow<ForbiddenRequestException> {
            resumeFacadeService.deleteResume(deleteRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_DELETE_FORBIDDEN
        }
      }
    }
  }

  describe("copyResume") {
    context("이력서를 복사하는 경우") {
      it("저장 후 복사된 이력서를 반환한다") {
        runTest {
          val copyRequest = createMockCopyResumeRequestDto()
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { resumeService.save(any()) } answers { generateCopiedResume() }

          val originalResume = generateResume()

          val profileImageDownloadUrl = "profile-image-download-url"
          val portfolioFileDownloadUrls = listOf("portfolio-file-download-url", "portfolio-file-download-url2")
          coEvery { fileService.generatePreSignedUrlToDownload(originalResume.userProfileImageUrl!!) } answers { profileImageDownloadUrl }
          coEvery { fileService.generatePreSignedUrlsToDownload(originalResume.portfolioFileUrls) } answers { portfolioFileDownloadUrls }

          val result = resumeFacadeService.copyResume(resume.userId, copyRequest)
          result.title shouldBe "[복사] ${originalResume.title.toString()}"
          result.isActive shouldBe originalResume.isActive
          result.userProfileImageUrl shouldBe profileImageDownloadUrl
          result.desiredJobs.first().name shouldBe originalResume.desiredJobs.first().name
          result.techStacks.first().name shouldBe originalResume.techStacks.first().name
          result.jobExperiences.first().companyName shouldBe originalResume.jobExperiences.first().companyName
          result.projectExperiences.first().projectName shouldBe originalResume.projectExperiences.first().projectName
          result.portfolioFileUrls shouldBe portfolioFileDownloadUrls
          result.portfolioUrl.first() shouldBe originalResume.portfolioUrl.first()
          result.selfDescription shouldBe originalResume.selfDescription
        }
      }

      it("권한이 없는 userId 로 복사 시도 시 UnauthorizedException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val updateRequest = createMockCopyResumeRequestDto()

          val exception = shouldThrow<ForbiddenRequestException> {
            resumeFacadeService.copyResume("no-auth-user", updateRequest)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_COPY_FORBIDDEN
        }
      }
    }
  }

  describe("changeResumeActiveStatus") {
    context("이력서의 프미팅 게시 여부를 ON 으로 변경하는 경우") {
      it("이력서의 프미팅 게시여부가 ON 으로 변경된다.") {
        runTest {
          val requestDto = createMockChangeResumeActiveRequestDto(true)
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          resumeFacadeService.changeResumeActiveStatus(resume.userId, requestDto)

          coVerify { resumeService.changeActive(resume, true) }
        }
      }

      it("권한이 없는 userId 로 상태 변경 시도 시 ForbiddenRequestException 발생시킨다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }

          val requestDto = createMockChangeResumeActiveRequestDto(true)

          val exception = shouldThrow<ForbiddenRequestException> {
            resumeFacadeService.changeResumeActiveStatus("no-auth-user", requestDto)
          }
          exception.errorCode shouldBe ErrorCode.RESUME_ACTIVE_CHANGE_FORBIDDEN
        }
      }
    }
  }

  describe("findResumeListByUserId") {
    context("사용자가 본인의 이력서 목록을 조회하는 경우") {
      it("이력서 목록을 반환한다.") {
        runTest {
          coEvery { resumeService.getAllByUserId(any()) } answers { resumeList }
          val result = resumeFacadeService.findResumeListByUserId(resumeList.first().userId)

          result.size shouldBe resumeList.size
        }
      }
    }
  }

  describe("addBookmark") {
    context("사용자가 타인의 이력서에 대해 북마크를 추가하는 경우") {
      it("북마크가 추가된다.") {
        runTest {
          coEvery { userService.getUserById(any()) } answers { user }
          coEvery { userService.update(any()) } answers { user }
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { resumeService.update(any()) } answers { resume }

          resumeFacadeService.addBookmark(userId, resumeId)

          user.bookmarkedResumes.size shouldBe 1
          resume.bookmarkers.size shouldBe 1
        }
      }
    }
  }

  describe("deleteBookmark") {
    context("사용자가 타인의 이력서에 대해 북마크를 추가하는 경우") {
      it("북마크가 추가된다.") {
        runTest {
          coEvery { userService.getUserById(any()) } answers { user }
          coEvery { userService.update(any()) } answers { user }
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { resumeService.update(any()) } answers { resume }

          resumeFacadeService.deleteBookmark(userId, resumeId)

          user.bookmarkedResumes.size shouldBe 0
          resume.bookmarkers.size shouldBe 0
        }
      }
    }
  }

  describe("getBookmarkedResumeList") {
    context("사용자가 북마크한 이력서 목록을 조회하는 경우") {
      it("북마크한 이력서 목록이 조회된다.") {
        runTest {
          user.addBookmarkForResume(resumeId)
          resume.isActive = true
          coEvery { userService.getUserById(any()) } answers { user }
          coEvery { resumeService.getResumeListByResumeId(any()) } answers { listOf(resume) }

          val result = resumeFacadeService.getBookmarkedResumeList(userId)

          result.size shouldBe 1
        }
      }
    }
  }

  describe("searchResumeSlice") {
    context("사용자가 조건에 따른 이력서 목록을 조회하는 경우") {
      val pageNumber = 0
      val pageSize = 10
      val userId = "1"
      val resumeListForSlice = generateMockResumeListForSlice().subList(0, pageSize + 1)

      it("조건에 따라서 Slice<Resume> 가 조회된다.") {
        runTest {
          coEvery { resumeService.searchSliceByFilter(any(), any(), any(), any(), any()) } answers {
            SliceResponse.of(
              resumeListForSlice.toMutableList(),
              PageRequest.of(pageNumber, pageSize)
            )
          }
          coEvery { fileService.generatePreSignedUrlToDownload(any()) } answers { "profile-url" }

          val result = resumeFacadeService.searchResumeSlice(
            userId,
            ResumeFilterType.ALL,
            "",
            ResumeOrderType.RECENT,
            PageRequest.of(pageNumber, pageSize)
          )

          result.size shouldBe pageSize
          result.isFirst shouldBe true
          result.isLast shouldBe false
          for (i in 1..<pageSize) {
            result.content.get(i).isMyBookmark shouldBe true
          }
        }
      }
    }
  }
})

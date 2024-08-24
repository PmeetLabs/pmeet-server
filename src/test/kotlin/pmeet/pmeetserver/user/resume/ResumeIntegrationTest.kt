package pmeet.pmeetserver.user.resume

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.BaseMongoDBTestForIntegration
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.resume.response.SearchedResumeResponseDto
import pmeet.pmeetserver.user.repository.UserRepository
import pmeet.pmeetserver.user.repository.job.JobRepository
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCopyResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCreateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeCopyResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockUpdateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateActiveResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeList
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeListForSlice
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateUpdatedResume
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import pmeet.pmeetserver.util.RestSliceImpl
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
class ResumeIntegrationTest : BaseMongoDBTestForIntegration() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  private lateinit var resumeFacadeService: ResumeFacadeService

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jobRepository: JobRepository

  @Autowired
  lateinit var techStackRepository: TechStackRepository

  @Autowired
  lateinit var resumeRepository: ResumeRepository

  @Autowired
  lateinit var userRepository: UserRepository

  lateinit var resume: Resume
  lateinit var activeResume: Resume
  lateinit var resumeId: String
  lateinit var resumeList: List<Resume>
  lateinit var resumeListForSlice: List<Resume>

  lateinit var user: User
  lateinit var userId: String

  override suspend fun beforeSpec(spec: Spec) {
    val job1 = Job(
      id = "job1",
      name = "Backend Developer",
    )

    val job2 = Job(
      id = "job2",
      name = "Frontend Developer",
    )

    val techStack1 = TechStack(
      id = "tech1",
      name = "Kotlin",
    )

    val techStack2 = TechStack(
      id = "tech2",
      name = "React",
    )

    resume = generateResume()

    activeResume = generateActiveResume()

    resumeList = generateResumeList()

    resumeListForSlice = generateResumeListForSlice()

    user = User(
      email = "testEmail@test.com",
      name = "testName",
      nickname = "nickname",
      phoneNumber = "phone",
      gender = Gender.MALE,
      profileImageUrl = "image-url"
    )
    withContext(Dispatchers.IO) {
      jobRepository.save(job1).block()
      jobRepository.save(job2).block()
      techStackRepository.save(techStack1).block()
      techStackRepository.save(techStack2).block()
      resumeRepository.save(resume).block()
      resumeRepository.save(activeResume).block()
      resumeRepository.saveAll(resumeList).collectList().block()
      resumeId = resume.id!!
      userRepository.save(user).block()
      userId = user.id!!
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      jobRepository.deleteAll().block()
      techStackRepository.deleteAll().block()
      resumeRepository.deleteAll().block()
      userRepository.deleteAll().block()
    }
  }

  init {
    describe("POST /api/v1/resumes") {
      context("인증된 유저의 이력서 생성 요청이 들어오면") {
        val userId = "1234"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val requestDto = createMockCreateResumeRequestDto()
        val resumeResponse = createMockResumeResponseDto()
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/resumes")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            val returnedResume = response.responseBody!!

            returnedResume.title shouldBe resumeResponse.title
            returnedResume.isActive shouldBe resumeResponse.isActive
            returnedResume.userId shouldBe resumeResponse.userId
            returnedResume.userName shouldBe resumeResponse.userName
            returnedResume.userGender shouldBe resumeResponse.userGender
            returnedResume.userBirthDate shouldBe resumeResponse.userBirthDate
            returnedResume.userPhoneNumber shouldBe resumeResponse.userPhoneNumber
            returnedResume.userEmail shouldBe resumeResponse.userEmail
            returnedResume.userProfileImageUrl shouldNotBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs shouldBe resumeResponse.desiredJobs
            returnedResume.techStacks shouldBe resumeResponse.techStacks
            returnedResume.jobExperiences shouldBe resumeResponse.jobExperiences
            returnedResume.projectExperiences shouldBe resumeResponse.projectExperiences
            returnedResume.portfolioFileUrl shouldNotBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
          }
        }
      }
    }

    describe("GET /api/v1/resumes") {
      context("인증된 유저의 이력서 조회 요청이 들어오면") {
        val userId = "1234"
        val resumeId = "resume-id"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val resumeResponse = createMockResumeResponseDto()

        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .get()
          .uri {
            it.path("/api/v1/resumes")
              .queryParam("resumeId", resumeId)
              .build()
          }
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { result ->
            val returnedResume = result.responseBody!!

            returnedResume.id shouldBe resumeResponse.id
            returnedResume.title shouldBe resumeResponse.title
            returnedResume.isActive shouldBe resumeResponse.isActive
            returnedResume.userId shouldBe returnedResume.userId
            returnedResume.userName shouldBe resumeResponse.userName
            returnedResume.userGender shouldBe resumeResponse.userGender
            returnedResume.userBirthDate shouldBe resumeResponse.userBirthDate
            returnedResume.userPhoneNumber shouldBe resumeResponse.userPhoneNumber
            returnedResume.userEmail shouldBe resumeResponse.userEmail
            returnedResume.userProfileImageUrl shouldNotBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs shouldBe resumeResponse.desiredJobs
            returnedResume.techStacks shouldBe resumeResponse.techStacks
            returnedResume.jobExperiences shouldBe resumeResponse.jobExperiences
            returnedResume.projectExperiences shouldBe resumeResponse.projectExperiences
            returnedResume.portfolioFileUrl shouldNotBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
          }
        }
      }
    }

    describe("GET api/v1/resumes/list") {
      context("인증된 유저가 이력서 목록 조회 요청이 들어오면") {
        val userId = "user2"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val resumeList = ResumeGenerator.createMockResumeResponseDtoList()

        val performRequest =
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .get()
            .uri {
              it.path("/api/v1/resumes/list")
                .build()
            }
            .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이력서를 반환한다") {
          performRequest.expectBody<List<ResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!

            returnedResumeList.size shouldBe resumeList.size
          }
        }
      }
    }

    context("인증되지 않은 유저의 이력서 목록 조회 요청이 들어오면") {
      val resumeId = "resume-id"
      val performRequest =
        webTestClient
          .get()
          .uri {
            it.path("/api/v1/resumes/list")
              .queryParam("resumeId", resumeId)
              .build()
          }
          .exchange()
      it("요청은 실패한다") {
        performRequest.expectStatus().isUnauthorized
      }
    }

    describe("PUT /api/v1/resumes") {
      context("인증된 유저의 이력서 수정 요청이 들어오면") {
        val requestDto = createMockUpdateResumeRequestDto()
        val requestTime = LocalDateTime.now().minusMinutes(1L)
        val userId = resume.userId
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val resumeResponse = generateUpdatedResume()
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/resumes")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("생성된 이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            val returnedResume = response.responseBody!!

            returnedResume.title shouldBe resumeResponse.title
            returnedResume.isActive shouldBe resumeResponse.isActive
            returnedResume.userProfileImageUrl shouldNotBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs.first().name shouldBe resumeResponse.desiredJobs.first().name
            returnedResume.techStacks.first().name shouldBe resumeResponse.techStacks.first().name
            returnedResume.jobExperiences.first().companyName shouldBe resumeResponse.jobExperiences.first().companyName
            returnedResume.projectExperiences.first().projectName shouldBe resumeResponse.projectExperiences.first().projectName
            returnedResume.portfolioFileUrl shouldNotBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
            returnedResume.updatedAt shouldBeAfter requestTime
            returnedResume.createdAt shouldBeAfter requestTime
          }
        }
      }
    }


    describe("DELETE api/v1/resumes") {
      context("인증된 유저의 이력서 삭제 요청이 들어오면") {
        val requestDto = ResumeGenerator.createMockDeleteResumeRequestDto()
        val userId = requestDto.userId
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .delete()
          .uri("/api/v1/resumes?id=${requestDto.id}")
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isNoContent
        }
      }
    }

    describe("POST /api/v1/resumes/copy") {
      context("인증된 유저의 이력서 복사 요청이 들어오면") {
        val requestDto = createMockCopyResumeRequestDto()
        val resumeResponse = createMockResumeCopyResponseDto()
        val userId = resumeResponse.userId
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/resumes/copy")
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            val returnedResume = response.responseBody!!

            returnedResume.title shouldBe resumeResponse.title
            returnedResume.isActive shouldBe resumeResponse.isActive
            returnedResume.userId shouldBe resumeResponse.userId
            returnedResume.userName shouldBe resumeResponse.userName
            returnedResume.userGender shouldBe resumeResponse.userGender
            returnedResume.userBirthDate shouldBe resumeResponse.userBirthDate
            returnedResume.userPhoneNumber shouldBe resumeResponse.userPhoneNumber
            returnedResume.userEmail shouldBe resumeResponse.userEmail
            returnedResume.userProfileImageUrl shouldNotBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs shouldBe resumeResponse.desiredJobs
            returnedResume.techStacks shouldBe resumeResponse.techStacks
            returnedResume.jobExperiences shouldBe resumeResponse.jobExperiences
            returnedResume.projectExperiences shouldBe resumeResponse.projectExperiences
            returnedResume.portfolioFileUrl shouldNotBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
          }
        }
      }
    }

    describe("PATCH api/v1/resumes/active") {
      context("인증된 유저이자 이력서의 소유주의 이력서 프미팅 게시 요청이 들어오면") {
        val requestDto = ResumeGenerator.createMockChangeResumeActiveRequestDto(true)
        val resumeResponse = createMockResumeCopyResponseDto()
        val userId = resumeResponse.userId
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .patch()
          .uri("/api/v1/resumes/active")
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }
      }
    }

    describe("POST api/v1/resumes/{resumeId}/bookmark") {
      context("인증된 유저이자 다른 사람의 이력서에 대한 북마크 등록") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/resumes/${resumeId}/bookmark")
          .exchange()
        it("해당 이력서에 대한 북마크가 등록된다") {
          performRequest.expectStatus().isOk
          resumeRepository.findById(resumeId).awaitSingle().bookmarkers.size shouldBe 1
          userRepository.findById(userId).awaitSingle().bookmarkedResumes.size shouldBe 1
        }
      }
    }

    describe("DELETE api/v1/resumes/{resumeId}/bookmark") {
      context("인증된 유저이자 다른 사람의 이력서에 대한 북마크 해제") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .delete()
          .uri("/api/v1/resumes/${resumeId}/bookmark")
          .exchange()
        it("해당 이력서에 대한 북마크가 삭제된다") {
          performRequest.expectStatus().isNoContent
          resumeRepository.findById(resumeId).awaitSingle().bookmarkers.size shouldBe 0
          userRepository.findById(userId).awaitSingle().bookmarkedResumes.size shouldBe 0
        }
      }
    }

    describe("GET api/v1/resumes/bookmark-list") {
      context("인증된 유저이자 북마크한 이력서 목록 조회") {

        resumeFacadeService.addBookmark(userId, activeResume.id!!)

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .get()
          .uri("/api/v1/resumes/bookmark-list")
          .exchange()

        it("북마크된 이력서 목록이 조회된다") {
          performRequest.expectStatus().isOk
          performRequest.expectBody<List<SearchedResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!
            returnedResumeList.size shouldBe 1
          }
        }
      }
    }

    describe("GET api/v1/resumes/search-slice") {
      context("인증된 유저의 이력서 조건 조회") {

        val userId = "userId"
        val pageSize = 10
        val pageNumber = 0

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).get().uri {
            it.path("/api/v1/resumes/search-slice").queryParam("filterType", ResumeFilterType.ALL)
              .queryParam("filterValue", "").queryParam("orderType", ResumeOrderType.RECENT)
              .queryParam("page", pageNumber).queryParam("size", pageSize).build()
          }.exchange()

        it("조건에 따라 조회된 Slice<Resume> 가 조회된다.") {
          performRequest.expectStatus().isOk
          performRequest.expectBody<RestSliceImpl<SearchedResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!
            returnedResumeList.size shouldBe pageSize
          }
        }

      }
    }
  }
}

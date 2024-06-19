package pmeet.pmeetserver.user.resume.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.controller.ResumeController
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeJobExperienceRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeJobRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeProjectExperienceRequestDto
import pmeet.pmeetserver.user.dto.resume.request.ResumeTechStackRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeJobExperienceResponseDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeProjectExperienceResponseDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import java.time.LocalDate

@WebFluxTest(ResumeController::class)
@Import(TestSecurityConfig::class)
internal class ResumeControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var resumeFacadeService: ResumeFacadeService

  init {
    describe("POST api/v1/resumes") {
      context("인증된 유저의 이력서 생성 요청이 들어오면") {
        val userId = "1234"
        val requestDto = createMockCreateResumeRequestDto()
        val responseDto = createMockResumeResponseDto()
        coEvery { resumeFacadeService.createResume(requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/resumes")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { resumeFacadeService.createResume(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 이력서 정보를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.title shouldBe responseDto.title
          }
        }
      }
      context("인증되지 않은 유저의 이력서 생성 요청이 들어오면") {
        val requestDto = createMockCreateResumeRequestDto()
        val performRequest =
          webTestClient
            .post()
            .uri("/api/v1/resumes")
            .bodyValue(requestDto)
            .exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("GET api/v1/resumes") {
      context("인증된 유저가 이력서 조회 요청이 들어오면") {
        val userId = "1234"
        val resumeId = "resume-id"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val resumeResponse = createMockResumeResponseDto()

        coEvery { resumeFacadeService.findResumeById(resumeId) } answers { resumeResponse }

        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri {
              it.path("/api/v1/resumes")
                .queryParam("resumeId", resumeId)
                .build()
            }
            .exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { resumeFacadeService.findResumeById(resumeId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { result ->
            val returnedResume = result.responseBody!!

            returnedResume.id shouldBe resumeResponse.id
            returnedResume.title shouldBe resumeResponse.title
            returnedResume.userName shouldBe resumeResponse.userName
            returnedResume.userGender shouldBe resumeResponse.userGender
            returnedResume.userBirthDate shouldBe resumeResponse.userBirthDate
            returnedResume.userPhoneNumber shouldBe resumeResponse.userPhoneNumber
            returnedResume.userEmail shouldBe resumeResponse.userEmail
            returnedResume.userProfileImageUrl shouldBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs shouldBe resumeResponse.desiredJobs
            returnedResume.techStacks shouldBe resumeResponse.techStacks
            returnedResume.jobExperiences shouldBe resumeResponse.jobExperiences
            returnedResume.projectExperiences shouldBe resumeResponse.projectExperiences
            returnedResume.portfolioFileUrl shouldBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
          }
        }
      }
    }

    context("인증되지 않은 유저의 이력서 조회 요청이 들어오면") {
      val resumeId = "resume-id"
      val performRequest =
        webTestClient
          .get()
          .uri {
            it.path("/api/v1/resumes")
              .queryParam("resumeId", resumeId)
              .build()
          }
          .exchange()
      it("요청은 실패한다") {
        performRequest.expectStatus().isUnauthorized
      }
    }
  }

  private fun createMockCreateResumeRequestDto(): CreateResumeRequestDto {
    return CreateResumeRequestDto(
      title = "Software Engineer",
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

  private fun createMockResumeResponseDto(): ResumeResponseDto {
    return ResumeResponseDto(
      id = "resume-id",
      title = "Software Engineer",
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
          companyName = "Project A",
          experiencePeriod = ExperienceYear.YEAR_01,
          responsibilities = "Led the project development"
        ),
        ResumeProjectExperienceResponseDto(
          companyName = "Project B",
          experiencePeriod = ExperienceYear.YEAR_02,
          responsibilities = "Contributed to backend services"
        )
      ),
      portfolioFileUrl = "http://example.com/portfolio.pdf",
      portfolioUrl = listOf("http://example.com/project1", "http://example.com/project2"),
      selfDescription = "Passionate software engineer with a focus on backend development."
    )
  }
}
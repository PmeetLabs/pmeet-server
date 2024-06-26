package pmeet.pmeetserver.user.resume

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.repository.job.JobRepository
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCreateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import java.time.LocalDate
import org.springframework.test.context.ActiveProfiles

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
class ResumeIntegrationTest : DescribeSpec() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jobRepository: JobRepository

  @Autowired
  lateinit var techStackRepository: TechStackRepository

  @Autowired
  lateinit var resumeRepository: ResumeRepository

  lateinit var resume: Resume

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

    withContext(Dispatchers.IO) {
      jobRepository.save(job1).block()
      jobRepository.save(job2).block()
      techStackRepository.save(techStack1).block()
      techStackRepository.save(techStack2).block()
      resumeRepository.save(resume).block()
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      jobRepository.deleteAll().block()
      techStackRepository.deleteAll().block()
      resumeRepository.deleteAll().block()
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

    describe("GET /api/v1/resumes") {
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

  companion object {
    @Container
    val mongoDBContainer = MongoDBContainer("mongo:latest").apply {
      withExposedPorts(27017)
      start()
    }

    init {
      System.setProperty(
        "spring.data.mongodb.uri",
        "mongodb://localhost:${mongoDBContainer.getMappedPort(27017)}/test"
      )
    }
  }

}

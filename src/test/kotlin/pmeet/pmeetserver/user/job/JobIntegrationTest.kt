package pmeet.pmeetserver.user.job

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
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.dto.job.request.CreateJobRequestDto
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.repository.job.JobRepository
import pmeet.pmeetserver.util.RestSliceImpl

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
internal class JobIntegrationTest : DescribeSpec() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jobRepository: JobRepository

  lateinit var job: Job

  override suspend fun beforeSpec(spec: Spec) {
    job = Job(
      name = "testName",
    )
    withContext(Dispatchers.IO) {
      jobRepository.save(job).block()
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      jobRepository.deleteAll().block()
    }
  }

  init {
    describe("POST /api/v1/jobs") {
      context("인증된 유저의 직무 생성 요청이 들어오면") {
        val userId = "1234"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val requestDto = CreateJobRequestDto("TestJob")
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/jobs")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 직무 정보를 반환한다") {
          performRequest.expectBody<JobResponseDto>().consumeWith { response ->
            response.responseBody?.name shouldBe requestDto.name
          }
        }
      }
    }

    describe("GET /api/v1/job/search") {
      val jobName = "TestJob"
      val userId = "1234"
      val pageNumber = 0
      val pageSize = 10
      withContext(Dispatchers.IO) {
        for (i in 1..pageSize * 2) {
          jobRepository.save(Job(name = jobName + i)).block()
        }
      }
      context("인증된 유저가 직무 이름과 페이지 정보가 주어지면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .get()
          .uri {
            it.path("/api/v1/jobs/search")
              .queryParam("name", jobName)
              .queryParam("page", pageNumber)
              .queryParam("size", pageSize)
              .build()
          }
          .accept(MediaType.APPLICATION_JSON)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이름을 포함하는 직무들을 Slice로 반환한다") {
          performRequest.expectBody<RestSliceImpl<JobResponseDto>>().consumeWith {
            it.responseBody?.content?.size shouldBe pageSize
            it.responseBody?.isFirst shouldBe true
            it.responseBody?.isLast shouldBe false
            it.responseBody?.size shouldBe pageSize
            it.responseBody?.number shouldBe pageNumber
            it.responseBody?.numberOfElements shouldBe pageSize
            it.responseBody?.content?.forEachIndexed { index, jobResponseDto ->
              jobResponseDto.name shouldBe jobName + (index + 1)
            }
            it.responseBody?.hasNext() shouldBe true
          }
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

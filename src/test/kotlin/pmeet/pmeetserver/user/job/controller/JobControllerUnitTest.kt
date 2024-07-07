package pmeet.pmeetserver.user.job.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.controller.JobController
import pmeet.pmeetserver.user.dto.job.request.CreateJobRequestDto
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.service.job.JobFacadeService
import pmeet.pmeetserver.util.RestSliceImpl

@WebFluxTest(JobController::class)
@Import(TestSecurityConfig::class)
internal class JobControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var jobFacadeService: JobFacadeService

  init {
    describe("POST api/v1/jobs") {
      context("인증된 유저의 직무 생성 요청이 들어오면") {
        val userId = "1234"
        val requestDto = CreateJobRequestDto("TestJob")
        val responseDto = JobResponseDto("1234", "TestJob")
        coEvery { jobFacadeService.createJob(requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/jobs")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { jobFacadeService.createJob(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 직무 정보를 반환한다") {
          performRequest.expectBody<JobResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.name shouldBe responseDto.name
          }
        }
      }
      context("인증되지 않은 유저의 직무 생성 요청이 들어오면") {
        val requestDto = CreateJobRequestDto("TestJob")
        val performRequest =
          webTestClient
            .post()
            .uri("/api/v1/jobs")
            .bodyValue(requestDto)
            .exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("GET api/v1/jobs/search") {
      context("인증된 유저가 직무 이름으로 직무 검색 요청이 들어오면") {
        val jobName = "TestJob"
        val userId = "1234"
        val jobId = "1234"
        val pageNumber = 0
        val pageSize = 10
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val jobResponses = mutableListOf<JobResponseDto>()
        for (i in 1..pageSize * 2) {
          jobResponses.add(JobResponseDto(jobId + (i - 1), jobName + (i - 1)))
        }

        val response = SliceImpl(
          jobResponses.subList(0, pageSize),
          PageRequest.of(pageNumber, pageSize),
          true
        )
        coEvery { jobFacadeService.searchJobByName(jobName, PageRequest.of(pageNumber, pageSize)) } answers { response }

        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri {
              it.path("/api/v1/jobs/search")
                .queryParam("name", jobName)
                .queryParam("page", pageNumber)
                .queryParam("size", pageSize)
                .build()
            }
            .exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { jobFacadeService.searchJobByName(jobName, PageRequest.of(pageNumber, pageSize)) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이름을 포함하는 직무들을 Slice로 반환한다") {
          performRequest.expectBody<RestSliceImpl<JobResponseDto>>().consumeWith {
            it.responseBody?.content?.size shouldBe pageSize
            it.responseBody?.isFirst shouldBe true
            it.responseBody?.isLast shouldBe false
            it.responseBody?.numberOfElements shouldBe pageSize
            it.responseBody?.size shouldBe pageSize
            it.responseBody?.number shouldBe pageNumber
            it.responseBody?.content?.forEachIndexed { index, jobResponseDto ->
              jobResponseDto.id shouldBe jobId + index
              jobResponseDto.name shouldBe jobName + index
            }
            it.responseBody?.hasNext() shouldBe true
          }
        }
      }

      context("인증되지 않은 유저가 직무 이름으로 직무 검색 요청이 들어오면") {
        val jobName = "TestJob"
        val pageNumber = 0
        val pageSize = 10
        val performRequest =
          webTestClient
            .get()
            .uri {
              it.path("/api/v1/jobs/search")
                .queryParam("name", jobName)
                .queryParam("page", pageNumber)
                .queryParam("size", pageSize)
                .build()
            }
            .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}

package pmeet.pmeetserver.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.dto.response.RecruitmentResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService
import java.time.LocalDateTime

@WebFluxTest(ProjectController::class)
@Import(TestSecurityConfig::class)
internal class ProjectControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var projectFacadeService: ProjectFacadeService

  init {
    describe("POST api/v1/projects") {
      context("인증된 유저의 프로젝트 생성 요청이 들어오면") {
        val userId = "1234"
        val requestDto = CreateProjectRequestDto(
          title = "TestProject",
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
          endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
          thumbNailUrl = "testThumbNailUrl",
          techStacks = listOf("testTechStack1", "testTechStack2"),
          recruitments = listOf(
            RecruitmentRequestDto(
              jobName = "testJobName",
              numberOfRecruitment = 1
            ),
            RecruitmentRequestDto(
              jobName = "testJobName2",
              numberOfRecruitment = 2
            )
          ),
          description = "testDescription"
        )
        val responseDto = ProjectResponseDto(
          id = "1234",
          title = "TestProject",
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
          endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
          thumbNailUrl = "testThumbNailUrl",
          techStacks = listOf("testTechStack1", "testTechStack2"),
          recruitments = listOf(
            RecruitmentResponseDto(
              jobName = "testJobName",
              numberOfRecruitment = 1
            ),
            RecruitmentResponseDto(
              jobName = "testJobName2",
              numberOfRecruitment = 2
            )
          ),
          description = "testDescription",
          userId = userId,
          bookMarkers = mutableListOf(),
          isCompleted = false,
          createdAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0)
        )

        coEvery { projectFacadeService.createProject(userId, requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/projects")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { projectFacadeService.createProject(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 프로젝트 정보를 반환한다") {
          performRequest.expectBody<ProjectResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.title shouldBe responseDto.title
            response.responseBody?.startDate shouldBe responseDto.startDate
            response.responseBody?.endDate shouldBe responseDto.endDate
            response.responseBody?.thumbNailUrl shouldBe responseDto.thumbNailUrl
            response.responseBody?.techStacks shouldBe responseDto.techStacks
            response.responseBody?.recruitments!!.size shouldBe responseDto.recruitments.size
            response.responseBody?.recruitments!!.forEachIndexed { index, recruitmentResponseDto ->
              recruitmentResponseDto.jobName shouldBe responseDto.recruitments[index].jobName
              recruitmentResponseDto.numberOfRecruitment shouldBe responseDto.recruitments[index].numberOfRecruitment
            }
            response.responseBody?.description shouldBe responseDto.description
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.bookMarkers shouldBe responseDto.bookMarkers
            response.responseBody?.isCompleted shouldBe responseDto.isCompleted
            response.responseBody?.createdAt shouldBe responseDto.createdAt
          }
        }
      }

      context("인증되지 않은 유저의 프로젝트 생성 요청이 들어오면") {
        val requestDto = CreateProjectRequestDto(
          title = "TestProject",
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
          endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
          thumbNailUrl = "testThumbNailUrl",
          techStacks = listOf("testTechStack1", "testTechStack2"),
          recruitments = listOf(
            RecruitmentRequestDto(
              jobName = "testJobName",
              numberOfRecruitment = 1
            ),
            RecruitmentRequestDto(
              jobName = "testJobName2",
              numberOfRecruitment = 2
            )
          ),
          description = "testDescription"
        )
        val performRequest =
          webTestClient
            .post()
            .uri("/api/v1/projects")
            .bodyValue(requestDto)
            .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}

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
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
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
      val requestDto = CreateProjectRequestDto(
        title = "TestTitlet",
        startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
        endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
        thumbNailUrl = "testThumbNailUrl",
        techStacks = listOf("testTechStack1", "testTechStack2"),
        recruitments = listOf(
          RecruitmentRequestDto(
            jobName = "testJobName1",
            numberOfRecruitment = 1
          ),
          RecruitmentRequestDto(
            jobName = "testJobName2",
            numberOfRecruitment = 2
          )
        ),
        description = "testDescription"
      )
      context("인증된 유저의 Project 생성 요청이 들어오면") {
        val userId = "1234"
        val responseDto = ProjectResponseDto(
          id = "testId",
          title = requestDto.title,
          startDate = requestDto.startDate,
          endDate = requestDto.endDate,
          thumbNailUrl = requestDto.thumbNailUrl,
          techStacks = requestDto.techStacks!!,
          recruitments = requestDto.recruitments.map { RecruitmentResponseDto(it.jobName, it.numberOfRecruitment) },
          description = requestDto.description,
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

        it("생성된 Project 정보를 반환한다") {
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

      context("인증되지 않은 유저의 Project 생성 요청이 들어오면") {
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

    describe("PUT api/v1/projects") {
      val requestDto = UpdateProjectRequestDto(
        id = "testId",
        title = "updateProject",
        startDate = LocalDateTime.of(2024, 7, 20, 0, 0, 0),
        endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
        thumbNailUrl = "updateThumbNailUrl",
        techStacks = listOf("updateTechStack1", "updateTechStack2"),
        recruitments = listOf(
          RecruitmentRequestDto(
            jobName = "updateJobName1",
            numberOfRecruitment = 3
          ),
          RecruitmentRequestDto(
            jobName = "updateJobName2",
            numberOfRecruitment = 4
          )
        ),
        description = "updateDescription"
      )
      context("인증된 유저의 Project 수정 요청이 들어오면") {
        val userId = "1234"
        val responseDto = ProjectResponseDto(
          id = requestDto.id,
          title = requestDto.title,
          startDate = requestDto.startDate,
          endDate = requestDto.endDate,
          thumbNailUrl = requestDto.thumbNailUrl,
          techStacks = requestDto.techStacks!!,
          recruitments = requestDto.recruitments.map { RecruitmentResponseDto(it.jobName, it.numberOfRecruitment) },
          description = requestDto.description,
          userId = userId,
          bookMarkers = mutableListOf(),
          isCompleted = false,
          createdAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0)
        )

        coEvery { projectFacadeService.updateProject(userId, requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .put()
            .uri("/api/v1/projects")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 수정한다") {
          coVerify(exactly = 1) { projectFacadeService.updateProject(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("수정된 Project 정보를 반환한다") {
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
      context("인증되지 않은 유저의 Project 수정 요청이 들어오면") {
        val performRequest =
          webTestClient
            .put()
            .uri("/api/v1/projects")
            .bodyValue(requestDto)
            .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("DELETE api/v1/projects/{projectId}") {
      val projectId = "testProjectId"
      context("인증된 유저의 Project 삭제 요청이 들어오면") {
        val userId = "1234"
        coEvery { projectFacadeService.deleteProject(userId, projectId) } answers { Unit }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .delete()
            .uri {
              it.path("/api/v1/projects")
                .path("/$projectId")
                .build()
            }
            .exchange()

        it("서비스를 통해 데이터를 삭제한다") {
          coVerify(exactly = 1) { projectFacadeService.deleteProject(userId, projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isNoContent
        }
      }

      context("인증되지 않은 유저의 Project 삭제 요청이 들어오면") {
        val performRequest =
          webTestClient
            .delete()
            .uri {
              it.path("/api/v1/projects")
                .path("/$projectId")
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

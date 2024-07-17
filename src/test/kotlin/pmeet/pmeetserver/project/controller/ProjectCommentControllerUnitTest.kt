package pmeet.pmeetserver.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.dto.request.comment.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService

@WebFluxTest(ProjectCommentController::class)
@Import(TestSecurityConfig::class)
internal class ProjectCommentControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var projectFacadeService: ProjectFacadeService

  init {
    describe("POST /api/v1/project-comments") {
      context("인증된 유저의 댓글 생성 요청이 들어오면") {
        val userId = "testUserId"
        val requestDto = CreateProjectCommentRequestDto(
          projectId = "testProjectId",
          parentCommentId = null,
          content = "testContent"
        )
        val responseDto = ProjectCommentResponseDto(
          id = "testCommentId",
          parentCommentId = null,
          projectId = "testProjectId",
          userId = userId,
          content = "testContent",
          likerIdList = listOf(),
          createdAt = LocalDateTime.of(2024, 7, 16, 0, 0, 0)
        )

        coEvery { projectFacadeService.createProjectComment(userId, requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/project-comments")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { projectFacadeService.createProjectComment(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 댓글 정보를 반환한다") {
          performRequest.expectBody<ProjectCommentResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.parentCommentId shouldBe responseDto.parentCommentId
            response.responseBody?.projectId shouldBe responseDto.projectId
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.content shouldBe responseDto.content
            response.responseBody?.likerIdList shouldBe responseDto.likerIdList
            response.responseBody?.createdAt shouldBe responseDto.createdAt
          }
        }
      }
    }
  }
}

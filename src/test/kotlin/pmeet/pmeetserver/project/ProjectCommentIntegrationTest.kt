package pmeet.pmeetserver.project

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.dto.request.comment.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentResponseDto
import pmeet.pmeetserver.project.repository.ProjectCommentRepository
import pmeet.pmeetserver.project.repository.ProjectRepository

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class)
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
internal class ProjectCommentIntegrationTest : DescribeSpec() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  private lateinit var projectRepository: ProjectRepository

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var projectCommentRepository: ProjectCommentRepository

  lateinit var project: Project
  lateinit var projectComment: ProjectComment
  lateinit var userId: String
  lateinit var projectId: String
  lateinit var commentId: String

  override suspend fun beforeSpec(spec: Spec) {
    userId = "testUserId"

    project = Project(
      userId = userId,
      title = "testTitle",
      startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
      endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
      thumbNailUrl = "testThumbNailUrl",
      techStacks = listOf("testTechStack1", "testTechStack2"),
      recruitments = emptyList(),
      description = "testDescription"
    )

    withContext(Dispatchers.IO) {
      projectRepository.save(project).block()
      projectId = projectRepository.findByUserId(project.userId).awaitFirst().id!!

      projectComment = ProjectComment(
        projectId = projectId,
        userId = userId,
        content = "testContent",
        isDeleted = false,
      )
      projectCommentRepository.save(projectComment).block()
      commentId = projectCommentRepository.findByProjectId(projectId).awaitFirst().id!!
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      projectRepository.deleteAll().block()
      projectCommentRepository.deleteAll().block()
    }
  }

  init {
    describe("POST /api/v1/project-comments") {
      context("인증된 유저의 댓글 생성 요청이 들어오면") {
        val requestDto = CreateProjectCommentRequestDto(
          projectId = projectId,
          parentCommentId = null,
          content = "testContent"
        )
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/project-comments")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 댓글 정보를 반환한다") {
          performRequest.expectBody<ProjectCommentResponseDto>().consumeWith { response ->
            val responseBody = response.responseBody
            responseBody?.id shouldNotBe null
            responseBody?.parentCommentId shouldBe requestDto.parentCommentId
            responseBody?.projectId shouldBe requestDto.projectId
            responseBody?.userId shouldBe userId
            responseBody?.content shouldBe requestDto.content
            responseBody?.likerIdList shouldBe mutableListOf()
            responseBody?.createdAt shouldNotBe null
            responseBody?.isDeleted shouldBe false
          }
        }
      }
    }

    describe("DELETE /api/v1/project-comments/{commentId}") {
      context("인증된 유저의 댓글 삭제 요청이 들어오면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .delete()
          .uri("/api/v1/project-comments/${commentId}")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("삭제된 댓글 정보를 반환한다") {
          performRequest.expectBody<ProjectCommentResponseDto>().consumeWith { response ->
            val responseBody = response.responseBody
            responseBody?.id shouldBe commentId
            responseBody?.content shouldBe "작성자가 삭제한 댓글입니다."
            responseBody?.isDeleted shouldBe true
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

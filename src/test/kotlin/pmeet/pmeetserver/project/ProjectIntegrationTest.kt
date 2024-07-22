package pmeet.pmeetserver.project

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime
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
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentWithChildResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.repository.ProjectCommentRepository
import pmeet.pmeetserver.project.repository.ProjectRepository

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
internal class ProjectIntegrationTest : DescribeSpec() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var projectRepository: ProjectRepository

  @Autowired
  lateinit var projectCommentRepository: ProjectCommentRepository

  lateinit var project: Project
  lateinit var userId: String
  lateinit var recruitments: List<Recruitment>
  lateinit var projectComment: ProjectComment
  lateinit var deletedProjectComment: ProjectComment
  lateinit var childProjectComment: ProjectComment

  override suspend fun beforeSpec(spec: Spec) {
    userId = "testUserId"
    recruitments = listOf(
      Recruitment(
        jobName = "testJobName",
        numberOfRecruitment = 1
      ),
      Recruitment(
        jobName = "testJobName2",
        numberOfRecruitment = 2
      )
    )

    project = Project(
      userId = userId,
      title = "testTitle",
      startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
      endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
      thumbNailUrl = "testThumbNailUrl",
      techStacks = listOf("testTechStack1", "testTechStack2"),
      recruitments = recruitments,
      description = "testDescription"
    )

    withContext(Dispatchers.IO) {
      projectRepository.save(project).block()

      projectComment = ProjectComment(
        projectId = project.id!!,
        userId = userId,
        content = "testContent",
        isDeleted = false,
      )
      projectCommentRepository.save(projectComment).block()

      deletedProjectComment = ProjectComment(
        projectId = project.id!!,
        userId = userId,
        content = "childContent",
        isDeleted = true,
      )
      projectCommentRepository.save(deletedProjectComment).block()
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      projectRepository.deleteAll().block()
      projectCommentRepository.deleteAll().block()
    }
  }

  init {
    describe("POST api/v1/projects") {
      context("인증된 유저의 Project 생성 요청이 들어오면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
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
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/projects")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 Project 정보를 반환한다") {
          performRequest.expectBody<ProjectResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldNotBe project.id
            response.responseBody?.title shouldBe requestDto.title
            response.responseBody?.startDate shouldBe requestDto.startDate
            response.responseBody?.endDate shouldBe requestDto.endDate
            response.responseBody?.thumbNailUrl shouldBe requestDto.thumbNailUrl
            response.responseBody?.techStacks shouldBe requestDto.techStacks
            response.responseBody?.recruitments!!.size shouldBe requestDto.recruitments.size
            response.responseBody?.recruitments!!.forEachIndexed { index, recruitmentResponseDto ->
              recruitmentResponseDto.jobName shouldBe requestDto.recruitments[index].jobName
              recruitmentResponseDto.numberOfRecruitment shouldBe requestDto.recruitments[index].numberOfRecruitment
            }
            response.responseBody?.description shouldBe requestDto.description
            response.responseBody?.userId shouldBe project.userId
            response.responseBody?.bookMarkers shouldBe project.bookMarkers
            response.responseBody?.isCompleted shouldBe project.isCompleted
            response.responseBody?.createdAt shouldNotBe null
          }
        }
      }
    }

    describe("PUT api/v1/projects") {
      context("인증된 유저의 Project 수정 요청이 들어오면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val requestDto = UpdateProjectRequestDto(
          id = project.id!!,
          title = "UpdateTitle",
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
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/projects")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("수정된 Project 정보를 반환한다") {
          performRequest.expectBody<ProjectResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe project.id
            response.responseBody?.title shouldBe requestDto.title
            response.responseBody?.startDate shouldBe requestDto.startDate
            response.responseBody?.endDate shouldBe requestDto.endDate
            response.responseBody?.thumbNailUrl shouldBe requestDto.thumbNailUrl
            response.responseBody?.techStacks shouldBe requestDto.techStacks
            response.responseBody?.recruitments!!.size shouldBe requestDto.recruitments.size
            response.responseBody?.recruitments!!.forEachIndexed { index, recruitmentResponseDto ->
              recruitmentResponseDto.jobName shouldBe requestDto.recruitments[index].jobName
              recruitmentResponseDto.numberOfRecruitment shouldBe requestDto.recruitments[index].numberOfRecruitment
            }
            response.responseBody?.description shouldBe requestDto.description
            response.responseBody?.userId shouldBe project.userId
            response.responseBody?.bookMarkers shouldBe project.bookMarkers
            response.responseBody?.isCompleted shouldBe project.isCompleted
            response.responseBody?.createdAt shouldNotBe null
          }
        }
      }
    }

    describe("DELETE api/v1/projects/{projectId}") {
      context("인증된 유저의 Project 삭제 요청이 들어오면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .delete()
          .uri("/api/v1/projects/${project.id}")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isNoContent
        }

        it("Project가 삭제된다") {
          withContext(Dispatchers.IO) {
            val deletedProject = projectRepository.findById(project.id!!).block()
            deletedProject shouldBe null
          }
        }

        it("ProjectComment가 삭제된다") {
          withContext(Dispatchers.IO) {
            val deletedProjectComment = projectCommentRepository.findById(projectComment.id!!).block()
            deletedProjectComment shouldBe null
          }
        }
      }
    }

    describe("GET api/v1/projects/{projectId}/comments") {
      context("Project Comment 전체 조회 요청이 들어오면") {
        val deletedProjectComment1 = ProjectComment(
          projectId = "testProjectId",
          userId = "testUserId",
          content = "deleted1",
          isDeleted = true
        )
        projectCommentRepository.save(deletedProjectComment1).block()

        val deletedProjectComment2 = ProjectComment(
          projectId = "testProjectId",
          userId = "testUserId",
          content = "deleted2",
          isDeleted = true
        )
        projectCommentRepository.save(deletedProjectComment2).block()

        val childProjectComment1 = ProjectComment(
          parentCommentId = deletedProjectComment1.id!!,
          projectId = "testProjectId",
          userId = "testUserId",
          content = "child",
          isDeleted = false
        )
        projectCommentRepository.save(childProjectComment1).block()

        val deletedChildProjectComment1 = ProjectComment(
          parentCommentId = deletedProjectComment1.id!!,
          projectId = "testProjectId",
          userId = "testUserId",
          content = "deletedChild",
          isDeleted = true
        )
        projectCommentRepository.save(deletedChildProjectComment1).block()

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val projectId = "testProjectId"

        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .get()
          .uri("/api/v1/projects/$projectId/comments")
          .accept(MediaType.APPLICATION_JSON)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("생성된 Project 정보를 반환한다") {
          performRequest.expectBody<List<ProjectCommentWithChildResponseDto>>().consumeWith { response ->
            response.responseBody?.get(0)?.id shouldBe deletedProjectComment1.id
            response.responseBody?.get(0)?.parentCommentId shouldBe deletedProjectComment1.parentCommentId
            response.responseBody?.get(0)?.projectId shouldBe deletedProjectComment1.projectId
            response.responseBody?.get(0)?.userId shouldBe deletedProjectComment1.userId
            response.responseBody?.get(0)?.content shouldBe deletedProjectComment1.content
            response.responseBody?.get(0)?.isDeleted shouldBe deletedProjectComment1.isDeleted

            response.responseBody?.get(0)?.childComments?.get(0)?.id shouldBe childProjectComment1.id
            response.responseBody?.get(0)?.childComments?.get(0)?.content shouldBe childProjectComment1.content
            response.responseBody?.get(0)?.childComments?.get(0)?.userId shouldBe childProjectComment1.userId
            response.responseBody?.get(0)?.childComments?.get(0)?.parentCommentId shouldBe childProjectComment1.parentCommentId
            response.responseBody?.get(0)?.childComments?.get(0)?.projectId shouldBe childProjectComment1.projectId
            response.responseBody?.get(0)?.childComments?.get(0)?.isDeleted shouldBe childProjectComment1.isDeleted
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

package pmeet.pmeetserver.project

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.repository.ProjectRepository
import java.time.LocalDateTime

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

  lateinit var project: Project
  lateinit var userId: String
  lateinit var recruitments: List<Recruitment>

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
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      projectRepository.deleteAll().block()
    }
  }

  init {
    describe("POST api/v1/projects") {
      context("인증된 유저의 프로젝트 생성 요청이 들어오면") {
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

        it("생성된 프로젝트 정보를 반환한다") {
          performRequest.expectBody<ProjectResponseDto>().consumeWith { response ->
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
            response.responseBody?.userId shouldBe userId
            response.responseBody?.bookMarkers shouldBe mutableListOf()
            response.responseBody?.isCompleted shouldBe false
            response.responseBody?.createdAt shouldNotBe null
            response.responseBody?.id shouldNotBe null
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

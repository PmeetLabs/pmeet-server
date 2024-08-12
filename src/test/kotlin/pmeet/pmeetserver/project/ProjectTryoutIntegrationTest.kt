package pmeet.pmeetserver.project

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
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.BaseMongoDBTestForIntegration
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.dto.request.tryout.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.request.tryout.ProjectTryoutResponseDto
import pmeet.pmeetserver.project.repository.ProjectRepository
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class)
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
internal class ProjectTryoutIntegrationTest : BaseMongoDBTestForIntegration() {

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    @Autowired
    private lateinit var resumeRepository: ResumeRepository

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    lateinit var project: Project
    lateinit var resume: Resume
    lateinit var userId: String
    lateinit var projectId: String

    override suspend fun beforeSpec(spec: Spec) {
        resume = generateResume()
        userId = resume.userId
        projectId = "testProjectId"

        project = Project(
            id = projectId,
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
            resumeRepository.save(resume).block()
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        withContext(Dispatchers.IO) {
            projectRepository.deleteAll().block()
            resumeRepository.deleteAll().block()
        }
    }

    init {
        describe("POST /api/v1/project-tryouts") {
            context("인증된 유저의 프로젝트에 대한 이력서 지원 요청이 들어오면") {
                val requestDto = CreateProjectTryoutRequestDto(
                    projectId = "testProjectId",
                    resumeId = resume.id!!,
                    positionName = "positionName",
                )
                val createdAt = LocalDateTime.now()
                val responseDto = ProjectTryoutResponseDto(
                    id = "testTryoutId",
                    resumeId = resume.id!!,
                    userId = userId,
                    projectId = "testProjectId",
                    userName = resume.userName,
                    userSelfDescription = resume.selfDescription.orEmpty(),
                    positionName = "positionName",
                    tryoutStatus = ProjectTryoutStatus.INREVIEW,
                    createdAt = createdAt
                )

                val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
                val performRequest =
                    webTestClient
                        .mutateWith(mockAuthentication(mockAuthentication))
                        .post()
                        .uri("/api/v1/project-tryouts")
                        .bodyValue(requestDto)
                        .exchange()

                it("요청은 성공한다") {
                    performRequest.expectStatus().isCreated
                }

                it("생성된 지원 정보를 반환한다") {
                    performRequest.expectBody<ProjectTryoutResponseDto>().consumeWith { response ->
                        response.responseBody?.projectId shouldBe responseDto.projectId
                        response.responseBody?.userId shouldBe responseDto.userId
                        response.responseBody?.userName shouldBe responseDto.userName
                        response.responseBody?.userSelfDescription shouldBe responseDto.userSelfDescription
                        response.responseBody?.tryoutStatus shouldBe responseDto.tryoutStatus
                    }
                }
            }
        }

        describe("GET /api/v1/project-tryouts/{projectId}") {
            context("인증된 유저의 프로젝트에 대한 이력서 지원 현황 조회 요청이 들어오면") {
                val createdAt = LocalDateTime.now()
                val responseDto = ProjectTryoutResponseDto(
                    id = "testTryoutId",
                    resumeId = resume.id!!,
                    userId = userId,
                    projectId = projectId,
                    userName = resume.userName,
                    userSelfDescription = resume.selfDescription.orEmpty(),
                    positionName = "positionName",
                    tryoutStatus = ProjectTryoutStatus.INREVIEW,
                    createdAt = createdAt
                )

                val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
                val performRequest =
                    webTestClient
                        .mutateWith(mockAuthentication(mockAuthentication))
                        .get()
                        .uri("/api/v1/project-tryouts/${projectId}")
                        .exchange()

                it("요청은 성공한다") {
                    performRequest.expectStatus().isOk
                }

                it("생성된 지원 정보를 반환한다") {
                    performRequest.expectBody<List<ProjectTryoutResponseDto>>().consumeWith { response ->
                        response.responseBody?.get(0)!!.projectId shouldBe responseDto.projectId
                        response.responseBody?.get(0)!!.userId shouldBe responseDto.userId
                        response.responseBody?.get(0)!!.userName shouldBe responseDto.userName
                        response.responseBody?.get(0)!!.userSelfDescription shouldBe responseDto.userSelfDescription
                        response.responseBody?.get(0)!!.tryoutStatus shouldBe responseDto.tryoutStatus
                    }
                }
            }
        }
    }
}

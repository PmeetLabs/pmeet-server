package pmeet.pmeetserver.project

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
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
import pmeet.pmeetserver.config.BaseMongoDBTestForIntegration
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectMember
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.repository.ProjectMemberRepository
import pmeet.pmeetserver.project.repository.ProjectRepository
import pmeet.pmeetserver.project.repository.ProjectTryoutRepository
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
class ProjectMemberIntegrationTest : BaseMongoDBTestForIntegration() {

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    @Autowired
    private lateinit var resumeRepository: ResumeRepository

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    @Autowired
    private lateinit var projectTryoutRepository: ProjectTryoutRepository

    @Autowired
    private lateinit var projectMemberRepository: ProjectMemberRepository

    @Autowired
    lateinit var webTestClient: WebTestClient

    lateinit var project: Project
    lateinit var resume: Resume
    lateinit var userId: String
    lateinit var projectId: String
    lateinit var projectTryout: ProjectTryout
    lateinit var projectTryoutId: String
    lateinit var projectMember: ProjectMember

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

        projectTryoutId = "testProjectTryoutId"

        projectTryout = ProjectTryout(
            id = projectTryoutId,
            resumeId = resume.id!!,
            userId = userId,
            userName = resume.userName,
            userSelfDescription = resume.selfDescription.orEmpty(),
            positionName = "testPositionName",
            tryoutStatus = ProjectTryoutStatus.INREVIEW,
            projectId = projectId,
            createdAt = LocalDateTime.now()
        )

        projectMember = projectTryout.createProjectMember()

        withContext(Dispatchers.IO) {
            projectRepository.save(project).block()
            resumeRepository.save(resume).block()
            projectTryoutRepository.save(projectTryout).block()
            projectMemberRepository.save(projectMember).block()
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        withContext(Dispatchers.IO) {
            projectRepository.deleteAll().block()
            resumeRepository.deleteAll().block()
            projectTryoutRepository.deleteAll().block()
            projectMemberRepository.deleteAll().block()
        }
    }

    init {
        describe("POST /api/v1/project-members") {
            context("인증된 유저의 프로젝트에 대한 멤버 삭제 요청이 들어오면") {
                val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
                val performRequest =
                    webTestClient
                        .mutateWith(mockAuthentication(mockAuthentication))
                        .delete()
                        .uri { uriBuilder ->
                            uriBuilder
                                .path("/api/v1/project-members")
                                .queryParam("projectid", projectId)
                                .queryParam("memberid", projectMember.id)
                                .build()
                        }
                        .exchange()

                it("요청은 성공한다") {
                    performRequest.expectStatus().isOk
                }
            }
        }
    }
}
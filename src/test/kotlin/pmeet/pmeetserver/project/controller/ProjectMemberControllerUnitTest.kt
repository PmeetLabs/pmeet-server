package pmeet.pmeetserver.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.service.ProjectFacadeService

@WebFluxTest(ProjectMemberController::class)
@Import(TestSecurityConfig::class)
class ProjectMemberControllerUnitTest : DescribeSpec() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var projectFacadeService: ProjectFacadeService

    init {

        describe("deleteProjectMember") {
            context("인증된 유저의 프로젝트 멤버에 대한 삭제 요청이 들어오면") {
                val userId = "1234"
                val projectId = "testProjectId"
                val memberId = "testMemberId"

                coEvery { projectFacadeService.deleteProjectMember(userId, projectId, memberId) } answers { Unit }

                val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
                val performRequest =
                    webTestClient
                        .mutateWith(mockAuthentication(mockAuthentication))
                        .delete()
                        .uri { uriBuilder ->
                            uriBuilder
                                .path("/api/v1/project-members")
                                .queryParam("projectid", projectId)
                                .queryParam("memberid", memberId)
                                .build()
                        }
                        .exchange()

                it("서비스를 통해 데이터를 업데이트한다.") {
                    coVerify(exactly = 1) { projectFacadeService.deleteProjectMember(userId, projectId, memberId) }
                }

                it("요청은 성공한다") {
                    performRequest.expectStatus().isOk
                }
            }
        }
    }

}
package pmeet.pmeetserver.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.dto.tryout.request.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.request.PatchProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.response.ProjectTryoutResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import java.time.LocalDateTime

@WebFluxTest(ProjectTryoutController::class)
@Import(TestSecurityConfig::class)
internal class ProjectTryoutControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var projectFacadeService: ProjectFacadeService

  init {
    describe("createProjectTryout") {
      context("인증된 유저의 프로젝트에 대한 이력서 지원 요청이 들어오면") {
        val resume = generateResume()
        val userId = resume.userId
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
          userName = "userName",
          userSelfDescription = "userSelfDescription",
          positionName = "positionName",
          tryoutStatus = ProjectTryoutStatus.INREVIEW,
          createdAt = createdAt
        )

        coEvery {
          projectFacadeService.createProjectTryout(
            userId,
            requestDto
          )
        } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/project-tryouts")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { projectFacadeService.createProjectTryout(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 게시글과 지원서 정보를 반환한다") {
          performRequest.expectBody<ProjectTryoutResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.projectId shouldBe responseDto.projectId
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.userName shouldBe responseDto.userName
            response.responseBody?.userSelfDescription shouldBe responseDto.userSelfDescription
            response.responseBody?.tryoutStatus shouldBe responseDto.tryoutStatus
            response.responseBody?.createdAt shouldBe responseDto.createdAt
          }
        }
      }
    }

    describe("getProjectTryoutList") {
      context("인증된 유저의 프로젝트에 대한 이력서 지원 현황 조회 요청이 들어오면") {
        val resume = generateResume()
        val userId = resume.userId
        val requestProjectId = "testProjectId"
        val createdAt = LocalDateTime.now()
        val responseDto = mutableListOf(
          ProjectTryoutResponseDto(
            id = "testTryoutId",
            resumeId = resume.id!!,
            userId = userId,
            projectId = requestProjectId,
            userName = "userName",
            userSelfDescription = "userSelfDescription",
            positionName = "positionName",
            tryoutStatus = ProjectTryoutStatus.INREVIEW,
            createdAt = createdAt
          )
        )

        coEvery {
          projectFacadeService.getProjectTryoutListByProjectId(
            userId,
            requestProjectId
          )
        } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri("/api/v1/project-tryouts/${requestProjectId}")
            .exchange()

        it("서비스를 통해 데이터를 조회한다.") {
          coVerify(exactly = 1) { projectFacadeService.getProjectTryoutListByProjectId(userId, requestProjectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("생성된 게시글과 지원서 정보를 반환한다") {
          performRequest.expectBody<List<ProjectTryoutResponseDto>>().consumeWith { response ->
            response.responseBody?.get(0)!!.projectId shouldBe responseDto.get(0).projectId
            response.responseBody?.get(0)!!.userId shouldBe responseDto.get(0).userId
            response.responseBody?.get(0)!!.userName shouldBe responseDto.get(0).userName
            response.responseBody?.get(0)!!.userSelfDescription shouldBe responseDto.get(0).userSelfDescription
            response.responseBody?.get(0)!!.tryoutStatus shouldBe responseDto.get(0).tryoutStatus
            response.responseBody?.get(0)!!.createdAt shouldBe responseDto.get(0).createdAt
          }
        }
      }
    }

    describe("patchProjectTryoutToAccepted") {
      context("인증된 유저의 프로젝트에 대한 지원 현황 합격 업데이트 요청이 들어오면") {
        val resume = generateResume()
        val userId = resume.userId
        val requestProjectId = "testProjectId"
        val requestTryoutId = "testTryoutId"
        val createdAt = LocalDateTime.now()
        val responseDto =
          ProjectTryoutResponseDto(
            id = requestTryoutId,
            resumeId = resume.id!!,
            userId = userId,
            projectId = requestProjectId,
            userName = "userName",
            userSelfDescription = "userSelfDescription",
            positionName = "positionName",
            tryoutStatus = ProjectTryoutStatus.INREVIEW,
            createdAt = createdAt
          )


        val requestDto = PatchProjectTryoutRequestDto(requestProjectId, requestTryoutId)

        coEvery {
          projectFacadeService.patchProjectTryoutStatusToAccept(
            userId,
            requestDto
          )
        } answers { responseDto }

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .patch()
            .uri("/api/v1/project-tryouts/accept")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 업데이트한다.") {
          coVerify(exactly = 1) { projectFacadeService.patchProjectTryoutStatusToAccept(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }
      }
    }

    describe("patchProjectTryoutToAccepted") {
      context("인증된 유저의 프로젝트에 대한 지원 현황 불합격 업데이트 요청이 들어오면") {
        val resume = generateResume()
        val userId = resume.userId
        val requestProjectId = "testProjectId"
        val requestTryoutId = "testTryoutId"
        val createdAt = LocalDateTime.now()
        val responseDto =
          ProjectTryoutResponseDto(
            id = requestTryoutId,
            resumeId = resume.id!!,
            userId = userId,
            projectId = requestProjectId,
            userName = "userName",
            userSelfDescription = "userSelfDescription",
            positionName = "positionName",
            tryoutStatus = ProjectTryoutStatus.INREVIEW,
            createdAt = createdAt
          )


        val requestDto = PatchProjectTryoutRequestDto(requestProjectId, requestTryoutId)

        coEvery {
          projectFacadeService.pathProjectTryoutStatusToReject(
            userId,
            requestDto
          )
        } answers { responseDto }

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .patch()
            .uri("/api/v1/project-tryouts/reject")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 업데이트한다.") {
          coVerify(exactly = 1) { projectFacadeService.pathProjectTryoutStatusToReject(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }
      }
    }

    describe("getAcceptedProjectTryoutList") {
      context("인증된 유저의 프로젝트에 대한 승인된 지원자 목록 조회 요청이 들어오면") {
        val userId = "testUserId"
        val projectId = "testProjectId"
        val createdAt = LocalDateTime.now()
        val acceptedTryouts = listOf(
          ProjectTryoutResponseDto(
            id = "testTryoutId1",
            resumeId = "resumeId1",
            userId = "userId1",
            projectId = projectId,
            userName = "userName1",
            userSelfDescription = "userSelfDescription1",
            positionName = "positionName1",
            tryoutStatus = ProjectTryoutStatus.ACCEPTED,
            createdAt = createdAt
          ),
          ProjectTryoutResponseDto(
            id = "testTryoutId2",
            resumeId = "resumeId2",
            userId = "userId2",
            projectId = projectId,
            userName = "userName2",
            userSelfDescription = "userSelfDescription2",
            positionName = "positionName2",
            tryoutStatus = ProjectTryoutStatus.ACCEPTED,
            createdAt = createdAt.plusHours(1)
          )
        )

        coEvery {
          projectFacadeService.getAcceptedProjectTryoutListByProjectId(userId, projectId)
        } returns acceptedTryouts

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .get()
          .uri("/api/v1/project-tryouts/$projectId/accept")
          .exchange()

        it("서비스를 통해 데이터를 조회한다") {
          coVerify(exactly = 1) { projectFacadeService.getAcceptedProjectTryoutListByProjectId(userId, projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("승인된 지원자 목록을 반환한다") {
          performRequest.expectBody<List<ProjectTryoutResponseDto>>().consumeWith { response ->
            val responseBody = response.responseBody
            responseBody shouldNotBe null
            responseBody!!.size shouldBe 2

            responseBody[0].id shouldBe acceptedTryouts[0].id
            responseBody[0].resumeId shouldBe acceptedTryouts[0].resumeId
            responseBody[0].userId shouldBe acceptedTryouts[0].userId
            responseBody[0].projectId shouldBe acceptedTryouts[0].projectId
            responseBody[0].userName shouldBe acceptedTryouts[0].userName
            responseBody[0].userSelfDescription shouldBe acceptedTryouts[0].userSelfDescription
            responseBody[0].positionName shouldBe acceptedTryouts[0].positionName
            responseBody[0].tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED
            responseBody[0].createdAt shouldBe acceptedTryouts[0].createdAt

            responseBody[1].id shouldBe acceptedTryouts[1].id
            responseBody[1].resumeId shouldBe acceptedTryouts[1].resumeId
            responseBody[1].userId shouldBe acceptedTryouts[1].userId
            responseBody[1].projectId shouldBe acceptedTryouts[1].projectId
            responseBody[1].userName shouldBe acceptedTryouts[1].userName
            responseBody[1].userSelfDescription shouldBe acceptedTryouts[1].userSelfDescription
            responseBody[1].positionName shouldBe acceptedTryouts[1].positionName
            responseBody[1].tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED
            responseBody[1].createdAt shouldBe acceptedTryouts[1].createdAt
          }
        }
      }

      context("인증되지 않은 유저의 요청이 들어오면") {
        val projectId = "testProjectId"

        val performRequest = webTestClient
          .get()
          .uri("/api/v1/project-tryouts/$projectId/accept")
          .exchange()

        it("인증 에러를 반환한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}

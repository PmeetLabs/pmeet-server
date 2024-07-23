package pmeet.pmeetserver.project.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.request.SearchProjectRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentWithChildResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectWithUserResponseDto
import pmeet.pmeetserver.project.dto.response.RecruitmentResponseDto
import pmeet.pmeetserver.project.dto.response.SearchProjectResponseDto
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.enums.ProjectSortProperty
import pmeet.pmeetserver.project.service.ProjectFacadeService
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.dto.response.UserResponseDtoInProject
import pmeet.pmeetserver.util.RestSliceImpl
import java.time.LocalDate
import java.time.LocalDateTime

@WebFluxTest(ProjectController::class)
@Import(TestSecurityConfig::class)
internal class ProjectControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var projectFacadeService: ProjectFacadeService

  init {
    describe("GET api/v1/projects") {
      context("유저의 프로젝트 조회 요청이 들어오면") {
        val userId = "1234"
        val projectId = "project-id"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val projectResponse = ProjectWithUserResponseDto(
          id = projectId,
          title = "title",
          startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
          endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
          thumbNailUrl = "testThumbNailUrl",
          techStacks = listOf("testTechStack1", "testTechStack2"),
          description = "testDescription",
          userId = userId,
          isCompleted = false,
          bookMarkers = emptyList(),
          userInfo = UserResponseDtoInProject(
            userId,
            "user-email",
            "user-phone",
            LocalDate.of(2021, 12, 31),
            Gender.MALE,
            "intro",
            "nickname",
            "image-url"
          ),
          createdAt = LocalDateTime.of(2021, 12, 31, 23, 59, 59)
        )

        coEvery { projectFacadeService.getProjectByProjectId(projectId) } answers { projectResponse }

        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri {
              it.path("/api/v1/projects")
                .queryParam("projectId", projectId)
                .build()
            }
            .exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { projectFacadeService.getProjectByProjectId(projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("프로젝트를 반환한다") {
          performRequest.expectBody<ProjectWithUserResponseDto>().consumeWith { result ->
            val returnedProject = result.responseBody!!

            returnedProject.id shouldBe projectResponse.id
            returnedProject.userId shouldBe projectResponse.userId
            returnedProject.title shouldBe projectResponse.title
            returnedProject.startDate shouldBe projectResponse.startDate
            returnedProject.endDate shouldBe projectResponse.endDate
            returnedProject.thumbNailUrl shouldBe projectResponse.thumbNailUrl
            returnedProject.description shouldBe projectResponse.description
            returnedProject.isCompleted shouldBe projectResponse.isCompleted
            returnedProject.userInfo.id shouldBe projectResponse.userInfo.id
            returnedProject.techStacks shouldBe projectResponse.techStacks
          }
        }
      }
    }

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
          bookmarked = false,
          isCompleted = false,
          createdAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0),
          updatedAt = LocalDateTime.of(2021, 6, 1, 0, 0, 0)
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
            response.responseBody?.recruitments?.size shouldBe responseDto.recruitments.size
            response.responseBody?.recruitments?.forEachIndexed { index, recruitmentResponseDto ->
              recruitmentResponseDto.jobName shouldBe responseDto.recruitments[index].jobName
              recruitmentResponseDto.numberOfRecruitment shouldBe responseDto.recruitments[index].numberOfRecruitment
            }
            response.responseBody?.description shouldBe responseDto.description
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.bookmarked shouldBe responseDto.bookmarked
            response.responseBody?.isCompleted shouldBe responseDto.isCompleted
            response.responseBody?.createdAt shouldBe responseDto.createdAt
            response.responseBody?.updatedAt shouldBe responseDto.updatedAt
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
          bookmarked = false,
          isCompleted = false,
          createdAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0),
          updatedAt = LocalDateTime.of(2021, 6, 1, 0, 0, 0)
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
            response.responseBody?.recruitments?.size shouldBe responseDto.recruitments.size
            response.responseBody?.recruitments?.forEachIndexed { index, recruitmentResponseDto ->
              recruitmentResponseDto.jobName shouldBe responseDto.recruitments[index].jobName
              recruitmentResponseDto.numberOfRecruitment shouldBe responseDto.recruitments[index].numberOfRecruitment
            }
            response.responseBody?.description shouldBe responseDto.description
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.bookmarked shouldBe responseDto.bookmarked
            response.responseBody?.isCompleted shouldBe responseDto.isCompleted
            response.responseBody?.createdAt shouldBe responseDto.createdAt
            response.responseBody?.updatedAt shouldBe responseDto.updatedAt
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

    describe("getProjectCommentList") {
      context("댓글 조회 요청이 들어오면") {
        val userId = "userId"
        val projectId = "testProjectId"
        val responseDto = listOf(
          ProjectCommentWithChildResponseDto(
            id = "testCommentId",
            parentCommentId = null,
            projectId = "testProjectId",
            userId = userId,
            content = "testContent",
            likerIdList = listOf(),
            createdAt = LocalDateTime.of(2024, 7, 16, 0, 0, 0),
            isDeleted = false,
            childComments = listOf(
              ProjectCommentResponseDto(
                id = "childCommentId",
                parentCommentId = "testCommentId",
                projectId = "testProjectId",
                userId = userId,
                content = "testContent",
                likerIdList = listOf(),
                createdAt = LocalDateTime.of(2024, 7, 16, 0, 0, 0),
                isDeleted = false,
              )
            )
          )
        )

        coEvery { projectFacadeService.getProjectCommentList(projectId) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri("/api/v1/projects/$projectId/comments")
            .exchange()

        it("서비스를 통해 데이터를 조회한다.") {
          coVerify(exactly = 1) { projectFacadeService.getProjectCommentList(projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("조회한 댓글 정보를 반환한다") {
          performRequest.expectBody<List<ProjectCommentWithChildResponseDto>>().consumeWith { response ->
            response.responseBody?.get(0)?.id shouldBe responseDto[0].id
            response.responseBody?.get(0)?.parentCommentId shouldBe responseDto[0].parentCommentId
            response.responseBody?.get(0)?.projectId shouldBe responseDto[0].projectId
            response.responseBody?.get(0)?.userId shouldBe responseDto[0].userId
            response.responseBody?.get(0)?.content shouldBe responseDto[0].content
            response.responseBody?.get(0)?.isDeleted shouldBe responseDto[0].isDeleted

            response.responseBody?.get(0)?.childComments?.get(0)?.id shouldBe responseDto[0].childComments[0].id
            response.responseBody?.get(0)?.childComments?.get(0)?.content shouldBe responseDto[0].childComments[0].content
            response.responseBody?.get(0)?.childComments?.get(0)?.userId shouldBe responseDto[0].childComments[0].userId
            response.responseBody?.get(0)?.childComments?.get(0)?.parentCommentId shouldBe responseDto[0].childComments[0].parentCommentId
            response.responseBody?.get(0)?.childComments?.get(0)?.projectId shouldBe responseDto[0].childComments[0].projectId
            response.responseBody?.get(0)?.childComments?.get(0)?.isDeleted shouldBe responseDto[0].childComments[0].isDeleted
          }
        }
      }
    }

    describe("GET /api/v1/projects/search-slice") {
      context("인증된 유저의 Project 검색 요청이 들어오면") {
        val userId = "1234"
        val filterType = ProjectFilterType.TITLE
        val filterValue = "test"
        val isCompleted = false
        val pageNumber = 0
        val pageSize = 8
        val sortBy = ProjectSortProperty.BOOK_MARKERS
        val direction = Sort.Direction.DESC
        val requestDto =
          SearchProjectRequestDto.of(isCompleted, filterType, filterValue, pageNumber, pageSize, sortBy, direction)

        val responseDtos = mutableListOf<SearchProjectResponseDto>()
        for (i in 1..20) {
          val responseDto = SearchProjectResponseDto(
            id = "testId$i",
            title = "testTitle$i",
            startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
            thumbNailUrl = "testThumbNailUrl$i",
            techStacks = listOf("testTechStack$i", "testTechStack${i + 20}"),
            jobNames = listOf(
              "testJobName$i", "testJobName${i + 20}"
            ),
            description = "testDescription$i",
            userId = "userId$i",
            bookmarked = true,
            isCompleted = false,
            createdAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0),
            updatedAt = LocalDateTime.of(2021, 5, 1, 0, 0, 0)
          )
          responseDtos.add(responseDto)
        }
        coEvery { projectFacadeService.searchProjectSlice(userId, requestDto) } answers {
          SliceImpl(responseDtos.subList(0, pageSize), requestDto.pageable, true)
        }

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .get()
          .uri { uriBuilder ->
            uriBuilder.path("/api/v1/projects/search-slice")
              .queryParam("isCompleted", isCompleted)
              .queryParam("filterType", filterType)
              .queryParam("filterValue", filterValue)
              .queryParam("page", pageNumber)
              .queryParam("size", pageSize)
              .queryParam("sortBy", sortBy)
              .queryParam("direction", direction)
              .build()
          }
          .exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { projectFacadeService.searchProjectSlice(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("검색된 Project 정보를 반환한다") {
          performRequest.expectBody<RestSliceImpl<SearchProjectResponseDto>>().consumeWith { response ->
            response.responseBody?.content?.size shouldBe pageSize
            response.responseBody?.content?.forEachIndexed { index, searchProjectResponseDto ->
              searchProjectResponseDto.id shouldBe responseDtos[index].id
              searchProjectResponseDto.title shouldBe responseDtos[index].title
              searchProjectResponseDto.startDate shouldBe responseDtos[index].startDate
              searchProjectResponseDto.endDate shouldBe responseDtos[index].endDate
              searchProjectResponseDto.thumbNailUrl shouldBe responseDtos[index].thumbNailUrl
              searchProjectResponseDto.techStacks shouldBe responseDtos[index].techStacks
              searchProjectResponseDto.jobNames shouldBe responseDtos[index].jobNames
              searchProjectResponseDto.description shouldBe responseDtos[index].description
              searchProjectResponseDto.userId shouldBe responseDtos[index].userId
              searchProjectResponseDto.bookmarked shouldBe responseDtos[index].bookmarked
              searchProjectResponseDto.isCompleted shouldBe responseDtos[index].isCompleted
              searchProjectResponseDto.createdAt shouldBe responseDtos[index].createdAt
              searchProjectResponseDto.updatedAt shouldBe responseDtos[index].updatedAt
            }
          }
        }
      }

      context("인증되지 않은 유저의 검색 요청이 들어오면") {
        val filterType = ProjectFilterType.TITLE
        val filterValue = "test"
        val isCompleted = false
        val pageNumber = 0
        val pageSize = 8
        val sortBy = ProjectSortProperty.BOOK_MARKERS
        val direction = Sort.Direction.DESC
        val performRequest = webTestClient
          .get()
          .uri { uriBuilder ->
            uriBuilder.path("/api/v1/projects/search-slice")
              .queryParam("isCompleted", isCompleted)
              .queryParam("filterType", filterType)
              .queryParam("filterValue", filterValue)
              .queryParam("page", pageNumber)
              .queryParam("size", pageSize)
              .queryParam("sortBy", sortBy)
              .queryParam("direction", direction)
              .build()
          }
          .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("PUT /api/v1/projects/{projectId}/bookmark") {
      val projectId = "testProjectId"
      context("인증된 유저의 Project 북마크 추가 요청이 들어오면") {
        val userId = "1234"
        coEvery { projectFacadeService.addBookmark(userId, projectId) } answers { Unit }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/projects/$projectId/bookmark")
          .exchange()

        it("서비스를 통해 북마크를 추가한다") {
          coVerify(exactly = 1) { projectFacadeService.addBookmark(userId, projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }
      }

      context("인증되지 않은 유저의 Project 북마크 추가 요청이 들어오면") {
        val performRequest = webTestClient
          .put()
          .uri("/api/v1/projects/$projectId/bookmark")
          .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("DELETE /api/v1/projects/{projectId}/bookmark") {
      val projectId = "testProjectId"
      context("인증된 유저의 Project 북마크 삭제 요청이 들어오면") {
        val userId = "1234"
        coEvery { projectFacadeService.deleteBookmark(userId, projectId) } answers { Unit }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest = webTestClient
          .mutateWith(mockAuthentication(mockAuthentication))
          .delete()
          .uri("/api/v1/projects/$projectId/bookmark")
          .exchange()

        it("서비스를 통해 북마크를 삭제한다") {
          coVerify(exactly = 1) { projectFacadeService.deleteBookmark(userId, projectId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isNoContent
        }
      }

      context("인증되지 않은 유저의 Project 북마크 삭제 요청이 들어오면") {
        val performRequest = webTestClient
          .delete()
          .uri("/api/v1/projects/$projectId/bookmark")
          .exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}

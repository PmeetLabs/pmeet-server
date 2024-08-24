package pmeet.pmeetserver.user.resume.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.controller.ResumeController
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.resume.response.SearchedResumeResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockChangeResumeActiveRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCopyResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockCreateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockDeleteResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeCopyResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeResponseDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockResumeResponseDtoList
import pmeet.pmeetserver.user.resume.ResumeGenerator.createMockUpdateResumeRequestDto
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateMockResumeListForSlice
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateUpdatedResume
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import pmeet.pmeetserver.util.RestSliceImpl

@WebFluxTest(ResumeController::class)
@Import(TestSecurityConfig::class)
internal class ResumeControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var resumeFacadeService: ResumeFacadeService

  init {
    describe("POST api/v1/resumes") {
      context("인증된 유저의 이력서 생성 요청이 들어오면") {
        val userId = "1234"
        val requestDto = createMockCreateResumeRequestDto()
        val responseDto = createMockResumeResponseDto()
        coEvery { resumeFacadeService.createResume(requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).post()
            .uri("/api/v1/resumes").bodyValue(requestDto).exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { resumeFacadeService.createResume(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 이력서 정보를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.title shouldBe responseDto.title
          }
        }
      }
      context("인증되지 않은 유저의 이력서 생성 요청이 들어오면") {
        val requestDto = createMockCreateResumeRequestDto()
        val performRequest = webTestClient.post().uri("/api/v1/resumes").bodyValue(requestDto).exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("GET api/v1/resumes") {
      context("인증된 유저가 이력서 조회 요청이 들어오면") {
        val userId = "1234"
        val resumeId = "resume-id"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val resumeResponse = createMockResumeResponseDto()

        coEvery { resumeFacadeService.findResumeById(resumeId) } answers { resumeResponse }

        val performRequest = webTestClient.mutateWith(mockAuthentication(mockAuthentication)).get().uri {
          it.path("/api/v1/resumes").queryParam("resumeId", resumeId).build()
        }.exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { resumeFacadeService.findResumeById(resumeId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { result ->
            val returnedResume = result.responseBody!!

            returnedResume.id shouldBe resumeResponse.id
            returnedResume.title shouldBe resumeResponse.title
            returnedResume.userName shouldBe resumeResponse.userName
            returnedResume.userGender shouldBe resumeResponse.userGender
            returnedResume.userBirthDate shouldBe resumeResponse.userBirthDate
            returnedResume.userPhoneNumber shouldBe resumeResponse.userPhoneNumber
            returnedResume.userEmail shouldBe resumeResponse.userEmail
            returnedResume.userProfileImageUrl shouldBe resumeResponse.userProfileImageUrl
            returnedResume.desiredJobs shouldBe resumeResponse.desiredJobs
            returnedResume.techStacks shouldBe resumeResponse.techStacks
            returnedResume.jobExperiences shouldBe resumeResponse.jobExperiences
            returnedResume.projectExperiences shouldBe resumeResponse.projectExperiences
            returnedResume.portfolioFileUrl shouldBe resumeResponse.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe resumeResponse.portfolioUrl
            returnedResume.selfDescription shouldBe resumeResponse.selfDescription
          }
        }
      }
    }

    context("인증되지 않은 유저의 이력서 조회 요청이 들어오면") {
      val resumeId = "resume-id"
      val performRequest = webTestClient.get().uri {
        it.path("/api/v1/resumes").queryParam("resumeId", resumeId).build()
      }.exchange()
      it("요청은 실패한다") {
        performRequest.expectStatus().isUnauthorized
      }
    }

    describe("GET api/v1/resumes/list") {
      context("인증된 유저가 이력서 목록 조회 요청이 들어오면") {
        val userId = "user2"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val resumeList = createMockResumeResponseDtoList()

        coEvery { resumeFacadeService.findResumeListByUserId(userId) } answers { resumeList }

        val performRequest = webTestClient.mutateWith(mockAuthentication(mockAuthentication)).get().uri {
          it.path("/api/v1/resumes/list").build()
        }.exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { resumeFacadeService.findResumeListByUserId(userId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이력서를 반환한다") {
          performRequest.expectBody<List<ResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!

            returnedResumeList.size shouldBe resumeList.size
          }
        }
      }
    }

    context("인증되지 않은 유저의 이력서 목록 조회 요청이 들어오면") {
      val resumeId = "resume-id"
      val performRequest = webTestClient.get().uri {
        it.path("/api/v1/resumes/list").queryParam("resumeId", resumeId).build()
      }.exchange()
      it("요청은 실패한다") {
        performRequest.expectStatus().isUnauthorized
      }
    }

    describe("PUT api/v1/resumes") {
      context("인증된 유저이자 이력서의 소유주의 이력서 수정 요청이 들어오면") {
        val requestDto = createMockUpdateResumeRequestDto()
        val userId = generateUpdatedResume().userId
        val responseDto = createMockResumeResponseDto()
        coEvery { resumeFacadeService.updateResume(userId, requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).put()
            .uri("/api/v1/resumes").bodyValue(requestDto).exchange()

        it("서비스를 통해 데이터를 업데이트한다") {
          coVerify(exactly = 1) { resumeFacadeService.updateResume(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("업데이트된 이력서 정보를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.title shouldBe responseDto.title
          }
        }
      }

      context("인증되지 않은 유저의 이력서 수정 요청이 들어오면") {
        val requestDto = createMockUpdateResumeRequestDto()
        val performRequest = webTestClient.put().uri("/api/v1/resumes").bodyValue(requestDto).exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("DELETE api/v1/resumes") {
      context("인증된 유저이자 이력서의 소유주의 이력서 삭제 요청이 들어오면") {
        val requestDto = createMockDeleteResumeRequestDto()
        val mockResume = createMockResumeResponseDto()
        val userId = requestDto.userId
        coEvery { resumeFacadeService.findResumeById(requestDto.id) } answers { mockResume }
        coEvery { resumeFacadeService.deleteResume(requestDto) } answers { }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).delete()
            .uri("/api/v1/resumes?id=${requestDto.id}").exchange()

        it("서비스를 통해 데이터를 업데이트한다") {
          coVerify(exactly = 1) { resumeFacadeService.deleteResume(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isNoContent
        }
      }

      context("인증되지 않은 유저의 이력서 삭제 요청이 들어오면") {
        val requestDto = createMockDeleteResumeRequestDto()
        val performRequest = webTestClient.delete().uri("/api/v1/resumes?id=${requestDto.id}").exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("POST api/v1/resumes/copy") {
      context("인증된 유저이자 이력서의 소유주의 이력서 복사 요청이 들어오면") {
        val requestDto = createMockCopyResumeRequestDto()
        val mockOriginalResume = createMockResumeResponseDto()
        val mockResume = createMockResumeCopyResponseDto()
        val userId = mockOriginalResume.userId
        coEvery { resumeFacadeService.copyResume(userId, requestDto) } answers { mockResume }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).post()
            .uri("/api/v1/resumes/copy").bodyValue(requestDto).exchange()

        it("서비스를 통해 이력서를 복사한다.") {
          coVerify(exactly = 1) { resumeFacadeService.copyResume(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("복사된 이력서를 반환한다") {
          performRequest.expectBody<ResumeResponseDto>().consumeWith { result ->
            val returnedResume = result.responseBody!!

            returnedResume.id shouldBe mockResume.id
            returnedResume.title shouldBe mockResume.title
            returnedResume.userName shouldBe mockResume.userName
            returnedResume.userGender shouldBe mockResume.userGender
            returnedResume.userBirthDate shouldBe mockResume.userBirthDate
            returnedResume.userPhoneNumber shouldBe mockResume.userPhoneNumber
            returnedResume.userEmail shouldBe mockResume.userEmail
            returnedResume.userProfileImageUrl shouldBe mockResume.userProfileImageUrl
            returnedResume.desiredJobs.first().name shouldBe mockResume.desiredJobs.first().name
            returnedResume.techStacks.first().name shouldBe mockResume.techStacks.first().name
            returnedResume.jobExperiences.first().companyName shouldBe mockResume.jobExperiences.first().companyName
            returnedResume.projectExperiences.first().projectName shouldBe mockResume.projectExperiences.first().projectName
            returnedResume.portfolioFileUrl shouldBe mockResume.portfolioFileUrl
            returnedResume.portfolioUrl shouldBe mockResume.portfolioUrl
            returnedResume.selfDescription shouldBe mockResume.selfDescription
          }
        }
      }
    }

    describe("PATCH api/v1/resumes/active") {
      context("인증된 유저이자 이력서의 소유주의 이력서 프미팅 게시 요청이 들어오면") {
        val requestDto = createMockChangeResumeActiveRequestDto(true)
        val mockResume = createMockResumeCopyResponseDto()
        val userId = mockResume.userId
        coEvery { resumeFacadeService.changeResumeActiveStatus(userId, requestDto) } answers { }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).patch()
            .uri("/api/v1/resumes/active").bodyValue(requestDto).exchange()

        it("서비스를 통해 데이터를 업데이트한다") {
          coVerify(exactly = 1) { resumeFacadeService.changeResumeActiveStatus(userId, requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }
      }

      context("인증되지 않은 유저의 이력서 삭제 요청이 들어오면") {
        val requestDto = createMockChangeResumeActiveRequestDto(true)
        val performRequest = webTestClient.patch().uri("/api/v1/resumes/active").bodyValue(requestDto).exchange()

        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("POST api/v1/resumes/{resumeId}/bookmark") {
      context("인증된 유저이자 다른 사람의 이력서에 대한 북마크 등록") {
        val resumeId = "resumeId"
        val userId = "userId"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).put()
          .uri("/api/v1/resumes/${resumeId}/bookmark").exchange()
        it("해당 이력서에 대한 북마크가 등록된다") {
          coVerify(exactly = 1) { resumeFacadeService.addBookmark(userId, resumeId) }
        }
      }
    }

    describe("DELETE api/v1/resumes/{resumeId}/bookmark") {
      context("인증된 유저이자 다른 사람의 이력서에 대한 북마크 해제") {
        val resumeId = "resumeId"
        val userId = "userId"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).delete()
          .uri("/api/v1/resumes/${resumeId}/bookmark").exchange()
        it("해당 이력서에 대한 북마크가 삭제된다") {
          coVerify(exactly = 1) { resumeFacadeService.deleteBookmark(userId, resumeId) }
        }
      }
    }

    describe("GET api/v1/resumes/bookmark-list") {
      context("인증된 유저이자 북마크한 이력서 목록 조회") {

        val userId = "userId"
        val resume = generateResume()
        resume.addBookmark(userId)
        val responseDto = listOf(SearchedResumeResponseDto.of(resume, "user_profile_image_url"))

        coEvery { resumeFacadeService.getBookmarkedResumeList(userId) } answers { responseDto }

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).get()
            .uri("/api/v1/resumes/bookmark-list").exchange()

        it("북마크된 이력서 목록이 조회된다") {
          performRequest.expectStatus().isOk
          performRequest.expectBody<List<SearchedResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!
            returnedResumeList.size shouldBe 1
          }
        }
      }
    }

    describe("GET api/v1/resumes/search-slice") {
      context("인증된 유저의 이력서 조건 조회") {

        val userId = "userId"
        val pageSize = 10
        val pageNumber = 0
        val resume =
          generateMockResumeListForSlice().subList(0, pageSize + 1).map { SearchedResumeResponseDto.of(it, null) }

        coEvery {
          resumeFacadeService.searchResumeSlice(
            ResumeFilterType.ALL, "", ResumeOrderType.POPULAR, PageRequest.of(pageNumber, pageSize)
          )
        } answers {
          SliceImpl(
            resume.toMutableList(), PageRequest.of(pageNumber, pageSize), true
          )
        }

        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication)).get().uri {
            it.path("/api/v1/resumes/search-slice").queryParam("filterType", ResumeFilterType.ALL)
              .queryParam("filterValue", "").queryParam("orderType", ResumeOrderType.POPULAR)
              .queryParam("page", pageNumber).queryParam("size", pageSize).build()
          }.exchange()

        coVerify(exactly = 1) {
          resumeFacadeService.searchResumeSlice(
            ResumeFilterType.ALL, "", ResumeOrderType.POPULAR, PageRequest.of(pageNumber, pageSize)
          )
        }

        it("조건에 따라 조회된 Slice<Resume> 가 조회된다.") {
          performRequest.expectStatus().isOk
          performRequest.expectBody<RestSliceImpl<SearchedResumeResponseDto>>().consumeWith { result ->
            val returnedResumeList = result.responseBody!!
            returnedResumeList.size shouldBe pageSize
          }
        }

      }
    }
  }
}

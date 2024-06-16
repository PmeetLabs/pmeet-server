package pmeet.pmeetserver.user.techStack.controller

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
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.controller.TechStackController
import pmeet.pmeetserver.user.dto.techStack.request.CreateTechStackRequestDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import pmeet.pmeetserver.user.service.techStack.TechStackFacadeService
import pmeet.pmeetserver.util.RestSliceImpl

@WebFluxTest(TechStackController::class)
@Import(TestSecurityConfig::class)
internal class TechStackControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var techStackFacadeService: TechStackFacadeService

  init {
    describe("POST api/v1/tech-stacks") {
      context("인증된 유저의 기술 스택 생성 요청이 들어오면") {
        val userId = "1234"
        val requestDto = CreateTechStackRequestDto("TestTechStack")
        val responseDto = TechStackResponseDto("1234", "TestTechStack")
        coEvery { techStackFacadeService.createTechStack(requestDto) } answers { responseDto }
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .post()
            .uri("/api/v1/tech-stacks")
            .bodyValue(requestDto)
            .exchange()

        it("서비스를 통해 데이터를 생성한다") {
          coVerify(exactly = 1) { techStackFacadeService.createTechStack(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 기술 스택 정보를 반환한다") {
          performRequest.expectBody<TechStackResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.name shouldBe responseDto.name
          }
        }
      }
      context("인증되지 않은 유저의 기술 스택 생성 요청이 들어오면") {
        val requestDto = CreateTechStackRequestDto("TestTechStack")
        val performRequest =
          webTestClient
            .post()
            .uri("/api/v1/tech-stacks")
            .bodyValue(requestDto)
            .exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }

    describe("GET api/v1/tech-stacks/search") {
      context("인증된 유저가 기술 스택 이름으로 기술 스택 검색 요청이 들어오면") {
        val techStackName = "TestTechStack"
        val userId = "1234"
        val techStackId = "1234"
        val pageNumber = 0
        val pageSize = 10
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val techStackResponses = mutableListOf<TechStackResponseDto>()
        for (i in 1..pageSize * 2) {
          techStackResponses.add(TechStackResponseDto(techStackId + (i - 1), techStackName + (i - 1)))
        }

        val response = SliceImpl(
          techStackResponses.subList(0, pageSize),
          PageRequest.of(pageNumber, pageSize),
          true
        )
        coEvery { techStackFacadeService.searchTechStackByName(techStackName, PageRequest.of(pageNumber, pageSize)) } answers { response }

        val performRequest =
          webTestClient
            .mutateWith(mockAuthentication(mockAuthentication))
            .get()
            .uri {
              it.path("/api/v1/tech-stacks/search")
                .queryParam("name", techStackName)
                .queryParam("page", pageNumber)
                .queryParam("size", pageSize)
                .build()
            }
            .exchange()

        it("서비스를 통해 데이터를 검색한다") {
          coVerify(exactly = 1) { techStackFacadeService.searchTechStackByName(techStackName, PageRequest.of(pageNumber, pageSize)) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이름을 포함하는 기술 스택들을 Slice로 반환한다") {
          performRequest.expectBody<RestSliceImpl<TechStackResponseDto>>().consumeWith {
            it.responseBody?.content?.size shouldBe pageSize
            it.responseBody?.isFirst shouldBe true
            it.responseBody?.isLast shouldBe false
            it.responseBody?.numberOfElements shouldBe pageSize
            it.responseBody?.size shouldBe pageSize
            it.responseBody?.number shouldBe pageNumber
            it.responseBody?.content?.forEachIndexed { index, techStackResponseDto ->
              techStackResponseDto.id shouldBe techStackId + index
              techStackResponseDto.name shouldBe techStackName + index
            }
            it.responseBody?.hasNext() shouldBe true
          }
        }
      }

      context("인증되지 않은 유저의 기술 스택 생성 요청이 들어오면") {
        val requestDto = CreateTechStackRequestDto("TestTechStack")
        val performRequest =
          webTestClient
            .post()
            .uri("/api/v1/tech-stacks")
            .bodyValue(requestDto)
            .exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}

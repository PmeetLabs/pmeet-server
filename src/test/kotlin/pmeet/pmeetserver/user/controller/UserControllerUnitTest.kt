package pmeet.pmeetserver.user.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.user.dto.response.UserSummaryResponseDto
import pmeet.pmeetserver.user.service.UserFacadeService


@WebFluxTest(UserController::class)
internal class UserControllerUnitTest : DescribeSpec() {

  @MockkBean
  lateinit var userFacadeService: UserFacadeService

  @Autowired
  lateinit var webTestClient: WebTestClient

  init {
    describe("getMySummaryInfo") {
      val userId = "1234"
      val expectedUserSummaryResponseDto = UserSummaryResponseDto(
        userId, "test@test.com", "test", true, "test/test.jpg"
      )
      coEvery { userFacadeService.getMySummaryInfo(any<String>()) } answers { expectedUserSummaryResponseDto }
      context("유저가 인증되면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val performRequest =
          webTestClient.mutateWith(mockAuthentication(mockAuthentication)).get().uri("/api/v1/users/me/summary")
            .exchange()

        it("서비스를 통해 데이터를 조회한다") {
          coVerify(exactly = 1) { userFacadeService.getMySummaryInfo(userId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("유저 정보를 반환한다") {
          performRequest.expectBody<UserSummaryResponseDto>().consumeWith { response ->
            response.responseBody?.id shouldBe userId
            response.responseBody?.email shouldBe expectedUserSummaryResponseDto.email
            response.responseBody?.nickname shouldBe expectedUserSummaryResponseDto.nickname
            response.responseBody?.isEmployed shouldBe expectedUserSummaryResponseDto.isEmployed
            response.responseBody?.profileImageUrl shouldBe expectedUserSummaryResponseDto.profileImageUrl
          }
        }
      }
      context("유저가 인증되지 않으면") {
        val performRequest = webTestClient.get().uri("/api/v1/users/me/summary").exchange()
        it("요청은 실패한다") {
          performRequest.expectStatus().isUnauthorized
        }
      }
    }
  }
}


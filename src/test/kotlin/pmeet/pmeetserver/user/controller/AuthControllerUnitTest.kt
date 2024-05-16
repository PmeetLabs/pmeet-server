package pmeet.pmeetserver.user.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.util.UriBuilder
import pmeet.pmeetserver.auth.service.oauth.OauthFacadeService
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.dto.request.CheckMailRequestDto
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SetPasswordRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.request.VerifyVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.response.UserJwtDto
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.service.UserFacadeService

@WebFluxTest(AuthController::class)
@Import(TestSecurityConfig::class)
internal class AuthControllerUnitTest : DescribeSpec() {

  @MockkBean
  lateinit var userFacadeService: UserFacadeService

  @MockkBean
  lateinit var oauthFacadeService: OauthFacadeService

  @Autowired
  lateinit var webTestClient: WebTestClient

  init {
    describe("POST /api/v1/auth/sign-up") {
      context("유저 생성 요청이 들어오면") {
        val requestDto: SignUpRequestDto = SignUpRequestDto("test@test.com", "test", "testpassword1@", "test")
        val responseDto: UserResponseDto =
          UserResponseDto("1234", null, "test@test.com", "test", "test", true, "test/test.jpg")
        coEvery { userFacadeService.save(requestDto) } answers { responseDto }
        val performRequest =
          webTestClient.post()
            .uri("/api/v1/auth/sign-up")
            .bodyValue(requestDto).exchange()
        it("유저 생성 서비스를 호출한다") {
          coVerify(exactly = 1) { userFacadeService.save(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 유저 정보를 반환한다") {
          performRequest.expectBody<UserResponseDto>().consumeWith { response ->
            response.responseBody?.provider shouldBe responseDto.provider
            response.responseBody?.id shouldBe responseDto.id
            response.responseBody?.email shouldBe responseDto.email
            response.responseBody?.nickname shouldBe responseDto.nickname
            response.responseBody?.isEmployed shouldBe responseDto.isEmployed
            response.responseBody?.profileImageUrl shouldBe responseDto.profileImageUrl
          }
        }
      }
    }

    describe("POST /api/v1/auth/sign-in") {
      context("로그인 요청이 들어오면") {
        val requestDto = SignInRequestDto("test@test", "testpassword1@")
        val responseDto = UserJwtDto.of("1234", "TestAccessToken", "TestRefreshToken")
        coEvery { userFacadeService.signIn(requestDto) } answers { responseDto }
        val performRequest = webTestClient.post().uri("/api/v1/auth/sign-in").bodyValue(requestDto).exchange()

        it("로그인 서비스를 호출한다") {
          coVerify(exactly = 1) { userFacadeService.signIn(requestDto) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("JWT 토큰 정보를 반환한다") {
          performRequest.expectBody<UserJwtDto>().consumeWith { response ->
            response.responseBody?.userId shouldBe responseDto.userId
            response.responseBody?.accessToken shouldBe responseDto.accessToken
            response.responseBody?.refreshToken shouldBe responseDto.refreshToken
          }
        }
      }
    }

    describe("GET /api/v1/auth/sign-in/google") {
      context("구글 로그인 요청이 들어오면") {
        val code = "TestCode"
        val state = "TestState"
        val responseDto = UserJwtDto.of("1234", "TestAccessToken", "TestRefreshToken")
        coEvery { oauthFacadeService.loginGoogleOauth(code) } answers { responseDto }
        val performRequest = webTestClient.get()
          .uri { uriBuilder: UriBuilder ->
            uriBuilder.path("/api/v1/auth/sign-in/google")
              .queryParam("code", code)
              .queryParam("state", state)
              .build()
          }.exchange()

        it("구글 로그인 서비스를 호출한다") {
          coVerify(exactly = 1) { oauthFacadeService.loginGoogleOauth(code) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isSeeOther
        }

        it("리다이렉트 URL을 반환한다") {
          performRequest.expectHeader().valueEquals(
            "Location",
            "https://pmeet.site/?userId=${responseDto.userId}&accessToken=${responseDto.accessToken}&refreshToken=${responseDto.refreshToken}"
          )
        }
      }
    }

    describe("GET /api/v1/auth/sign-in/naver") {
      context("네이버 로그인 요청이 들어오면") {
        val code = "TestCode"
        val state = "TestState"
        val responseDto = UserJwtDto.of("1234", "TestAccessToken", "TestRefreshToken")
        coEvery { oauthFacadeService.loginNaverOauth(code, state) } answers { responseDto }
        val performRequest = webTestClient.get()
          .uri { uriBuilder: UriBuilder ->
            uriBuilder.path("/api/v1/auth/sign-in/naver")
              .queryParam("code", code)
              .queryParam("state", state)
              .build()
          }.exchange()

        it("네이버 로그인 서비스를 호출한다") {
          coVerify(exactly = 1) { oauthFacadeService.loginNaverOauth(code, state) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isSeeOther
        }

        it("리다이렉트 URL을 반환한다") {
          performRequest.expectHeader().valueEquals(
            "Location",
            "https://pmeet.site/?userId=${responseDto.userId}&accessToken=${responseDto.accessToken}&refreshToken=${responseDto.refreshToken}"
          )
        }
      }

      describe("GET /api/v1/auth/sign-in/kakao") {
        context("카카오 로그인 요청이 들어오면") {
          val code = "TestCode"
          val responseDto = UserJwtDto.of("1234", "TestAccessToken", "TestRefreshToken")
          coEvery { oauthFacadeService.loginKakaoOauth(code) } answers { responseDto }
          val performRequest = webTestClient.get()
            .uri { uriBuilder: UriBuilder ->
              uriBuilder.path("/api/v1/auth/sign-in/kakao")
                .queryParam("code", code)
                .build()
            }.exchange()

          it("카카오 로그인 서비스를 호출한다") {
            coVerify(exactly = 1) { oauthFacadeService.loginKakaoOauth(code) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isSeeOther
          }

          it("리다이렉트 URL을 반환한다") {
            performRequest.expectHeader().valueEquals(
              "Location",
              "https://pmeet.site/?userId=${responseDto.userId}&accessToken=${responseDto.accessToken}&refreshToken=${responseDto.refreshToken}"
            )
          }
        }
      }
      describe("POST /api/v1/auth/nickname/duplicate") {
        context("닉네임 중복 검사 요청이 들어오면") {
          val requestDto = CheckNickNameRequestDto("test")
          coEvery { userFacadeService.isDuplicateNickName(requestDto) } answers { true }
          val performRequest =
            webTestClient.post().uri("/api/v1/auth/nickname/duplicate").bodyValue(requestDto).exchange()

          it("닉네임 중복 검사 서비스를 호출한다") {
            coVerify(exactly = 1) { userFacadeService.isDuplicateNickName(requestDto) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isOk
          }

          it("중복 여부를 반환한다") {
            performRequest.expectBody<Boolean>().consumeWith { response ->
              response.responseBody shouldBe true
            }
          }
        }
      }

      describe("POST /api/v1/auth/mail/duplicate") {
        context("이메일 중복 검사 요청이 들어오면") {
          val requestDto = CheckMailRequestDto("test@test.com")
          coEvery { userFacadeService.isDuplicateMail(requestDto) } answers { true }
          val performRequest =
            webTestClient.post().uri("/api/v1/auth/mail/duplicate").bodyValue(requestDto).exchange()

          it("이메일 중복 검사 서비스를 호출한다") {
            coVerify(exactly = 1) { userFacadeService.isDuplicateMail(requestDto) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isOk
          }

          it("중복 여부를 반환한다") {
            performRequest.expectBody<Boolean>().consumeWith { response ->
              response.responseBody shouldBe true
            }
          }
        }
      }

      describe("POST /api/v1/auth/verification-code") {
        context("이메일 인증 요청이 들어오면") {
          val requestDto = SendVerificationCodeRequestDto("test@test.com")
          coEvery { userFacadeService.sendVerificationCode(requestDto) } answers { true }
          val performRequest =
            webTestClient.post().uri("/api/v1/auth/verification-code").bodyValue(requestDto).exchange()

          it("이메일 인증 코드 전송 서비스를 호출한다") {
            coVerify(exactly = 1) { userFacadeService.sendVerificationCode(requestDto) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isOk
          }

          it("인증 코드 전송 여부를 반환한다") {
            performRequest.expectBody<Boolean>().consumeWith { response ->
              response.responseBody shouldBe true
            }
          }
        }
      }

      describe("POST /api/v1/auth/verification-code/verify") {
        context("인증 코드 검증 요청이 들어오면") {
          val requestDto = VerifyVerificationCodeRequestDto("test@test.com", "test12")
          coEvery { userFacadeService.verifyVerificationCode(requestDto) } answers { true }
          val performRequest =
            webTestClient.post().uri("/api/v1/auth/verification-code/verify").bodyValue(requestDto).exchange()

          it("인증 코드 검증 서비스를 호출한다") {
            coVerify(exactly = 1) { userFacadeService.verifyVerificationCode(requestDto) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isOk
          }

          it("인증 코드 검증 여부를 반환한다") {
            performRequest.expectBody<Boolean>().consumeWith { response ->
              response.responseBody shouldBe true
            }
          }
        }
      }

      describe("POST /api/v1/auth/password") {
        context("비밀번호 변경 요청이 들어오면") {
          val requestDto = SetPasswordRequestDto("test@test.com", "testpassword1@", "testpassword1@")
          coEvery { userFacadeService.setPassword(requestDto) } answers { true }
          val performRequest =
            webTestClient.put().uri("/api/v1/auth/password").bodyValue(requestDto).exchange()

          it("비밀번호 변경 서비스를 호출한다") {
            coVerify(exactly = 1) { userFacadeService.setPassword(requestDto) }
          }

          it("요청은 성공한다") {
            performRequest.expectStatus().isOk
          }

          it("비밀번호 변경 여부를 반환한다") {
            performRequest.expectBody<Boolean>().consumeWith { response ->
              response.responseBody shouldBe true
            }
          }
        }
      }
    }
  }
}

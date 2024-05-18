package pmeet.pmeetserver.user

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.response.UserSummaryResponseDto
import pmeet.pmeetserver.user.repository.UserRepository


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
internal class UserIntegrationTest : DescribeSpec() {
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

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var userRepository: UserRepository

  private val user = User(
    email = "test@example.com",
    name = "Test User",
    nickname = "testuser",
    password = "password"
  )

  lateinit var userId: String

  override suspend fun beforeSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      userRepository.save(user).block()
      userId = userRepository.findByNickname(user.nickname).awaitFirst().id!!
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      userRepository.deleteAll().block()
    }
  }

  init {
    describe("GET /api/v1/users/me/summary") {
      context("인증된 사용자의 요약 정보를 가져올 때") {
        it("사용자의 요약 정보를 반환한다") {
          val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

          webTestClient.mutateWith(mockAuthentication(mockAuthentication)).get()
            .uri("/api/v1/users/me/summary")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody<UserSummaryResponseDto>()
            .consumeWith {
              val userSummary = it.responseBody!!
              userSummary.email shouldBe user.email
              userSummary.nickname shouldBe user.nickname
            }
        }
      }
    }
  }
}

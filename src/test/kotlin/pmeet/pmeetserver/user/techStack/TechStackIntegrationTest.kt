package pmeet.pmeetserver.user.techStack

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
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.techStack.request.CreateTechStackRequestDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import pmeet.pmeetserver.util.RestSliceImpl

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
class TechStackIntegrationTest : DescribeSpec() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var techStackRepository: TechStackRepository

  lateinit var techStack: TechStack

  override suspend fun beforeSpec(spec: Spec) {
    techStack = TechStack(
      name = "testName",
    )
    withContext(Dispatchers.IO) {
      techStackRepository.save(techStack).block()
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      techStackRepository.deleteAll().block()
    }  
  }


  init {
    describe("POST /api/v1/tech-stacks") {
      context("인증된 유저의 기술 스택 생성 요청이 들어오면") {
        val userId = "1234"
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
        val requestDto = CreateTechStackRequestDto("TestTechStack")
        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .post()
          .uri("/api/v1/tech-stacks")
          .accept(MediaType.APPLICATION_JSON)
          .bodyValue(requestDto)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isCreated
        }

        it("생성된 기술 스택 정보를 반환한다") {
          performRequest.expectBody<TechStackResponseDto>().consumeWith { response ->
            response.responseBody?.name shouldBe requestDto.name
          }
        }
      }
    }

    describe("GET /api/v1/tech-stacks/search") {
      val techStackName = "TestTechStack"
      val userId = "1234"
      val pageNumber = 0
      val pageSize = 10
      withContext(Dispatchers.IO) {
        for (i in 1..pageSize * 2) {
          techStackRepository.save(TechStack(name = techStackName + i)).block()
        }
      }
      context("인증된 유저가 기술 스택 이름과 페이지 정보가 주어지면") {
        val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)

        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .get()
          .uri {
            it.path("/api/v1/tech-stacks/search")
              .queryParam("name", techStackName)
              .queryParam("page", pageNumber)
              .queryParam("size", pageSize)
              .build()
          }
          .accept(MediaType.APPLICATION_JSON)
          .exchange()

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("이름을 포함하는 기술 스택들을 Slice로 반환한다") {
          performRequest.expectBody<RestSliceImpl<TechStackResponseDto>>().consumeWith {
            it.responseBody?.content?.size shouldBe pageSize
            it.responseBody?.isFirst shouldBe true
            it.responseBody?.isLast shouldBe false
            it.responseBody?.size shouldBe pageSize
            it.responseBody?.number shouldBe pageNumber
            it.responseBody?.numberOfElements shouldBe pageSize
            it.responseBody?.content?.forEachIndexed { index, techStackResponseDto ->
              techStackResponseDto.name shouldBe techStackName + (index + 1)
            }
            it.responseBody?.hasNext() shouldBe true
          }
        }
      }
    }
  }
}

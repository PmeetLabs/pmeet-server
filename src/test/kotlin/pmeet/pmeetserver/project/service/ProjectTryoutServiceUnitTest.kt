package pmeet.pmeetserver.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.repository.ProjectTryoutRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectTryoutServiceUnitTest : DescribeSpec({
  val testDispatcher = StandardTestDispatcher()

  val projectTryoutRepository = mockk<ProjectTryoutRepository>(relaxed = true)

  lateinit var projectTryoutService: ProjectTryoutService
  lateinit var projectTryout: ProjectTryout
  lateinit var userId: String

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectTryoutService = ProjectTryoutService(projectTryoutRepository)

    userId = "testUserId"

    projectTryout = ProjectTryout(
      projectId = "testProjectId",
      userId = userId,
      resumeId = "resumeId",
      createdAt = LocalDateTime.now()
    )
    ReflectionTestUtils.setField(projectTryout, "id", "testTryoutId")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("댓글 정보가 주어지면") {
      it("저장 후 프로젝트 지원 이력을 반환한다") {
        runTest {
          every { projectTryoutRepository.save(any()) } answers { Mono.just(projectTryout) }

          val result = projectTryoutService.save(projectTryout)

          result.id shouldBe projectTryout.id
          result.resumeId shouldBe projectTryout.resumeId
          result.userId shouldBe projectTryout.userId
          result.projectId shouldBe projectTryout.projectId
        }
      }
    }
  }
})

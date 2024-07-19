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
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.repository.ProjectCommentRepository
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
internal class ProjectCommentServiceUnitTest : DescribeSpec({
  val testDispatcher = StandardTestDispatcher()

  val projectCommentRepository = mockk<ProjectCommentRepository>(relaxed = true)

  lateinit var projectCommentService: ProjectCommentService
  lateinit var projectComment: ProjectComment
  lateinit var userId: String

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectCommentService = ProjectCommentService(projectCommentRepository)

    userId = "testUserId"

    projectComment = ProjectComment(
      projectId = "testProjectId",
      userId = userId,
      content = "testContent",
      isDeleted = false
    )
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("댓글 정보가 주어지면") {
      it("저장 후 댓글을 반환한다") {
        runTest {
          every { projectCommentRepository.save(any()) } answers { Mono.just(projectComment) }

          val result = projectCommentService.save(projectComment)

          result.id shouldBe projectComment.id
          result.parentCommentId shouldBe projectComment.parentCommentId
          result.content shouldBe projectComment.content
          result.userId shouldBe projectComment.userId
          result.projectId shouldBe projectComment.projectId
          result.isDeleted shouldBe false
        }
      }
    }
  }
})

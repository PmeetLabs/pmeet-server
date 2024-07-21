package pmeet.pmeetserver.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
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
    ReflectionTestUtils.setField(projectComment, "id", "testCommentId")
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

  describe("getProjectCommentById") {
    context("commentId가 주어지면") {
      it("댓글을 반환한다") {
        runTest {
          val commentId = projectComment.id!!
          every { projectCommentRepository.findById(commentId) } answers { Mono.just(projectComment) }

          val result = projectCommentService.getProjectCommentById(commentId)

          result.id shouldBe projectComment.id
          result.parentCommentId shouldBe projectComment.parentCommentId
          result.content shouldBe projectComment.content
          result.userId shouldBe projectComment.userId
          result.projectId shouldBe projectComment.projectId
          result.isDeleted shouldBe false
        }
      }

      it("존재하는 댓글이 없을 경우 Exception") {
        runTest {
          val commentId = projectComment.id!!
          every { projectCommentRepository.findById(commentId) } answers { Mono.empty() }

          shouldThrow<EntityNotFoundException> {
            projectCommentService.getProjectCommentById(commentId)
          }.errorCode shouldBe ErrorCode.PROJECT_COMMENT_NOT_FOUND
        }
      }
    }
  }

  describe("deleteAllByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당 프로젝트 ID에 해당하는 댓글을 삭제한다") {
        runTest {
          every { projectCommentRepository.deleteByProjectId(any()) } answers { Mono.empty() }

          projectCommentService.deleteAllByProjectId(projectComment.projectId)

          verify(exactly = 1) { projectCommentRepository.deleteByProjectId(projectComment.projectId) }
        }
      }
    }
  }
})

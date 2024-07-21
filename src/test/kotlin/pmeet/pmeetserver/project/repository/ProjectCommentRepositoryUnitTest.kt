package pmeet.pmeetserver.project.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.project.domain.ProjectComment

@ExperimentalCoroutinesApi
@DataMongoTest
internal class ProjectCommentRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : DescribeSpec({

//  isolationMode = IsolationMode.InstancePerLeaf
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)

  val projectRepositoryCustom = ProjectCommentRepositoryCustomImpl(template)
  val projectCommentRepository = factory.getRepository(ProjectCommentRepository::class.java, projectRepositoryCustom)

  lateinit var projectComment: ProjectComment

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectComment = ProjectComment(
      id = "testId",
      projectId = "testProjectId",
      userId = "testUserId",
      content = "testContent"
    )
    projectCommentRepository.save(projectComment).block()
    projectComment = ProjectComment(
      projectId = "testProjectId",
      userId = "testUserId",
      content = "testContent",
      isDeleted = false
    )
    projectCommentRepository.save(projectComment).block()

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    projectCommentRepository.deleteAll().block()
  }

  describe("deleteByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당하는 프로젝트 ID의 댓글을 삭제한다") {
        projectCommentRepository.deleteByProjectId(projectComment.projectId).block()

        // 삭제됐는지 확인
        val deletedComment = projectCommentRepository.findById(projectComment.id!!).block()
        deletedComment shouldBe null
      }
    }
  }

  describe("findCommentsByProjectIdWithChild") {
    context("projectId가 주어지면") {
      it("댓글(답글 포함) 반환") {
        runTest {
          val projectComment2 = ProjectComment(
            projectId = "testProjectId",
            userId = "testUserId",
            content = "testContent2",
            isDeleted = false
          )
          projectCommentRepository.save(projectComment2).block()

          val childProjectComment1 = ProjectComment(
            parentCommentId = projectComment2.id!!,
            projectId = "testProjectId",
            userId = "testUserId",
            content = "child",
            isDeleted = false
          )
          projectCommentRepository.save(childProjectComment1).block()

          val result =
            projectCommentRepository.findCommentsByProjectIdWithChild(projectComment.projectId).collectList().block()

          result?.size shouldBe 2
        }
      }
    }
  }
}) {
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
}

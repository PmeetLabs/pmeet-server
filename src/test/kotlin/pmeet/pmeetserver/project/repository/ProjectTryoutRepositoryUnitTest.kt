package pmeet.pmeetserver.project.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
@DataMongoTest
internal class ProjectTryoutRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate,
) : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)

  val projectTryoutRepository = factory.getRepository(ProjectTryoutRepository::class.java)

  lateinit var projectTryout: ProjectTryout

  beforeSpec {

    Dispatchers.setMain(testDispatcher)

    projectTryout = ProjectTryout(
      projectId = "testProjectId",
      userId = "testUserId",
      resumeId = "resumeId",
      userName = "testUserName",
      positionName = "testPosition",
      tryoutStatus = ProjectTryoutStatus.INREVIEW,
      createdAt = LocalDateTime.of(2024, 7, 23, 0, 0, 0)
    )
    projectTryoutRepository.save(projectTryout).block()
  }

  afterSpec {
    Dispatchers.resetMain()
    projectTryoutRepository.deleteAll().block()
  }

  describe("deleteByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당하는 프로젝트 ID의 지원 이력을 삭제한다") {
        projectTryoutRepository.deleteByProjectId(projectTryout.projectId).block()

        // 삭제됐는지 확인
        val deletedTryout = projectTryoutRepository.findById(projectTryout.id!!).block()
        deletedTryout shouldBe null
      }
    }
  }
})

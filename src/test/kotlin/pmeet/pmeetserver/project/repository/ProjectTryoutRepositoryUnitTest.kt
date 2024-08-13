package pmeet.pmeetserver.project.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import pmeet.pmeetserver.config.BaseMongoDBTestForRepository
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectTryoutRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate,
) : BaseMongoDBTestForRepository({

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

  describe("findAllByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당하는 프로젝트 ID의 지원 이력 목록을 조회한다") {
        val result = projectTryoutRepository.findAllByProjectId(projectTryout.projectId).collectList().awaitSingle()

        result?.size shouldBe 1
        result.get(0).projectId shouldBe projectTryout.projectId
        result.get(0).tryoutStatus shouldBe projectTryout.tryoutStatus
        result.get(0).userName shouldBe projectTryout.userName
        result.get(0).userId shouldBe projectTryout.userId
        result.get(0).createdAt shouldBe projectTryout.createdAt
        result.get(0).resumeId shouldBe projectTryout.resumeId
      }
    }
  }
})

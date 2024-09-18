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

  val projectId = "testProjectId"
  lateinit var projectTryout: ProjectTryout
  lateinit var acceptedProjectTryout: ProjectTryout
  lateinit var rejectedProjectTryout: ProjectTryout

  beforeSpec {

    Dispatchers.setMain(testDispatcher)

    projectTryout = ProjectTryout(
      projectId = projectId,
      userId = "testUserId",
      resumeId = "resumeId",
      userName = "testUserName",
      userSelfDescription = "userSelfDescription",
      positionName = "testPosition",
      tryoutStatus = ProjectTryoutStatus.INREVIEW,
      createdAt = LocalDateTime.of(2024, 7, 23, 0, 0, 0)
    )

    acceptedProjectTryout = ProjectTryout(
      projectId = projectId,
      userId = "testUserId2",
      resumeId = "resumeId2",
      userName = "testUserName2",
      userSelfDescription = "userSelfDescription2",
      positionName = "testPosition2",
      tryoutStatus = ProjectTryoutStatus.ACCEPTED,
      createdAt = LocalDateTime.of(2024, 7, 24, 0, 0, 0)
    )

    rejectedProjectTryout = ProjectTryout(
      projectId = projectId + "3",
      userId = "testUserId2",
      resumeId = "resumeId2",
      userName = "testUserName2",
      userSelfDescription = "userSelfDescription2",
      positionName = "testPosition2",
      tryoutStatus = ProjectTryoutStatus.REJECTED,
      createdAt = LocalDateTime.of(2024, 7, 24, 0, 0, 0)
    )
    projectTryoutRepository.saveAll(listOf(projectTryout, acceptedProjectTryout, rejectedProjectTryout)).collectList()
      .block()
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

        result?.size shouldBe 2
        result.get(0).projectId shouldBe projectTryout.projectId
        result.get(0).tryoutStatus shouldBe projectTryout.tryoutStatus
        result.get(0).userName shouldBe projectTryout.userName
        result.get(0).userSelfDescription shouldBe projectTryout.userSelfDescription
        result.get(0).userId shouldBe projectTryout.userId
        result.get(0).createdAt shouldBe projectTryout.createdAt
        result.get(0).resumeId shouldBe projectTryout.resumeId
      }
    }
  }

  describe("findAllByProjectIdAndTryoutStatus") {
    context("프로젝트 ID와 TryoutStatus가 주어지면") {
      it("해당하는 프로젝트 ID와 TryoutStatus의 지원 이력 목록을 조회한다") {
        val result = projectTryoutRepository.findAllByProjectIdAndTryoutStatusIsOrderByUpdatedAtDesc(
          projectTryout.projectId,
          ProjectTryoutStatus.ACCEPTED
        ).collectList().awaitSingle()

        result.size shouldBe 1
        result[0].projectId shouldBe acceptedProjectTryout.projectId
        result[0].tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED
        result[0].userName shouldBe acceptedProjectTryout.userName
        result[0].userSelfDescription shouldBe acceptedProjectTryout.userSelfDescription
        result[0].userId shouldBe acceptedProjectTryout.userId
        result[0].createdAt shouldBe acceptedProjectTryout.createdAt
        result[0].resumeId shouldBe acceptedProjectTryout.resumeId
      }
    }
  }

})

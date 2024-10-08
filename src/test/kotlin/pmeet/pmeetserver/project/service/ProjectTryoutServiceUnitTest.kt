package pmeet.pmeetserver.project.service

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
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.repository.ProjectTryoutRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectTryoutServiceUnitTest : DescribeSpec({
  val testDispatcher = StandardTestDispatcher()

  val projectTryoutRepository = mockk<ProjectTryoutRepository>(relaxed = true)

  lateinit var projectTryoutService: ProjectTryoutService
  lateinit var projectTryout: ProjectTryout
  lateinit var acceptedProjectTryout: ProjectTryout
  lateinit var projectTryoutForList: ProjectTryout
  lateinit var userId: String

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectTryoutService = ProjectTryoutService(projectTryoutRepository)

    userId = "testUserId"

    projectTryout = ProjectTryout(
      projectId = "testProjectId",
      userId = userId,
      resumeId = "resumeId",
      userName = "testUserName",
      userSelfDescription = "testSelfDescription",
      positionName = "testPosition",
      tryoutStatus = ProjectTryoutStatus.INREVIEW,
      createdAt = LocalDateTime.now()
    )

    acceptedProjectTryout = ProjectTryout(
      projectId = "testProjectId",
      userId = "testUserId2",
      resumeId = "resumeId2",
      userName = "testUserName2",
      userSelfDescription = "userSelfDescription2",
      positionName = "testPosition2",
      tryoutStatus = ProjectTryoutStatus.ACCEPTED,
      createdAt = LocalDateTime.of(2024, 7, 24, 0, 0, 0)
    )

    ReflectionTestUtils.setField(projectTryout, "id", "testTryoutId")

    projectTryoutForList = ProjectTryout(
      projectId = "testProjectId",
      userId = userId,
      resumeId = "resumeId",
      userName = "testUserName2",
      userSelfDescription = "testSelfDescription",
      positionName = "testPosition",
      tryoutStatus = ProjectTryoutStatus.ACCEPTED,
      createdAt = LocalDateTime.now()
    )
    ReflectionTestUtils.setField(projectTryout, "id", "testTryoutIdForList")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("게시글과 지원서 정보가 주어지면") {
      it("저장 후 프로젝트 지원 이력을 반환한다") {
        runTest {
          every { projectTryoutRepository.save(any()) } answers { Mono.just(projectTryout) }

          val result = projectTryoutService.save(projectTryout)

          result.id shouldBe projectTryout.id
          result.resumeId shouldBe projectTryout.resumeId
          result.userId shouldBe projectTryout.userId
          result.userName shouldBe projectTryout.userName
          result.userSelfDescription shouldBe projectTryout.userSelfDescription
          result.positionName shouldBe projectTryout.positionName
          result.projectId shouldBe projectTryout.projectId
        }
      }
    }
  }

  describe("deleteAllByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당 프로젝트 지원 이력을 삭제한다") {
        runTest {
          every { projectTryoutRepository.deleteByProjectId(any()) } answers { Mono.empty() }

          projectTryoutService.deleteAllByProjectId(projectTryout.projectId)

          verify(exactly = 1) { projectTryoutRepository.deleteByProjectId(projectTryout.projectId) }
        }
      }
    }
  }

  describe("findAllByProjectId") {
    context("프로젝트 ID 가 주어지면") {
      it("해당 프로젝트에 지원한 모든 지원 목록을 반환한다") {
        runTest {
          every { projectTryoutRepository.findAllByProjectId(any()) } answers {
            Flux.fromIterable(
              mutableListOf(
                projectTryout,
                projectTryoutForList
              )
            )
          }

          val result = projectTryoutService.findAllByProjectId(projectTryout.projectId)

          result.size shouldBe 2

          result.get(0).id shouldBe projectTryout.id
          result.get(0).resumeId shouldBe projectTryout.resumeId
          result.get(0).userId shouldBe projectTryout.userId
          result.get(0).userName shouldBe projectTryout.userName
          result.get(0).userSelfDescription shouldBe projectTryout.userSelfDescription
          result.get(0).positionName shouldBe projectTryout.positionName
          result.get(0).projectId shouldBe projectTryout.projectId

          result.get(1).id shouldBe projectTryoutForList.id
          result.get(1).resumeId shouldBe projectTryoutForList.resumeId
          result.get(1).userId shouldBe projectTryoutForList.userId
          result.get(1).userName shouldBe projectTryoutForList.userName
          result.get(1).userSelfDescription shouldBe projectTryout.userSelfDescription
          result.get(1).positionName shouldBe projectTryoutForList.positionName
          result.get(1).projectId shouldBe projectTryoutForList.projectId
        }
      }
    }
  }

  describe("updateTryoutStatus") {
    context("지원 현황에 대한 업데이트 요청이 주어지면") {
      it("지원 현황의 업데이트 결과가 반환된다.") {
        runTest {
          every { projectTryoutRepository.findById(projectTryout.id!!) } answers { Mono.just(projectTryout) }
          every { projectTryoutRepository.save(any()) } answers { Mono.just(projectTryout) }

          val result = projectTryoutService.updateTryoutStatus(projectTryout.id!!, ProjectTryoutStatus.ACCEPTED)

          result.tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED
        }
      }
    }
  }

  describe("deleteTryout") {
    context("지원 현황에 대한 삭제 요청이 주어지면") {
      it("지원 현황이 삭제된다.") {
        runTest {
          every { projectTryoutRepository.deleteById(projectTryout.id!!) } answers { Mono.empty() }

          projectTryoutService.deleteTryout(projectTryout.id!!)

          verify(exactly = 1) { projectTryoutRepository.deleteById(projectTryout.id!!) }
        }
      }
    }
  }

  describe("findAllAcceptedTryoutByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당 프로젝트의 승인된 지원 목록을 반환한다") {
        runTest {
          val projectId = acceptedProjectTryout.projectId
          every {
            projectTryoutRepository.findAllByProjectIdAndTryoutStatusIsOrderByUpdatedAtDesc(
              projectId,
              ProjectTryoutStatus.ACCEPTED
            )
          } returns Flux.fromIterable(listOf(acceptedProjectTryout))

          val result = projectTryoutService.findAllAcceptedTryoutByProjectId(projectId)

          result.size shouldBe 1
          result.all { it.tryoutStatus == ProjectTryoutStatus.ACCEPTED } shouldBe true
          result.all { it.projectId == projectId } shouldBe true

          verify(exactly = 1) {
            projectTryoutRepository.findAllByProjectIdAndTryoutStatusIsOrderByUpdatedAtDesc(
              projectId,
              ProjectTryoutStatus.ACCEPTED
            )
          }
        }
      }
    }
  }
})

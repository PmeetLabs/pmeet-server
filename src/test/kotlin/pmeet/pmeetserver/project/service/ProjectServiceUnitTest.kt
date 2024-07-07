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
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.repository.ProjectRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectServiceUnitTest : DescribeSpec({

//  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val projectRepository = mockk<ProjectRepository>(relaxed = true)

  lateinit var projectService: ProjectService
  lateinit var project: Project
  lateinit var userId: String
  lateinit var recruitments: List<Recruitment>

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectService = ProjectService(projectRepository)

    userId = "testUserId"
    recruitments = listOf(
      Recruitment(
        jobName = "testJobName",
        numberOfRecruitment = 1
      ),
      Recruitment(
        jobName = "testJobName2",
        numberOfRecruitment = 2
      )
    )

    project = Project(
      userId = userId,
      title = "testTitle",
      startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
      endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
      thumbNailUrl = "testThumbNailUrl",
      techStacks = listOf("testTechStack1", "testTechStack2"),
      recruitments = recruitments,
      description = "testDescription"
    )
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("프로젝트 정보가 주어지면") {
      it("저장 후 프로젝트를 반환한다") {
        runTest {
          every { projectRepository.save(any()) } answers { Mono.just(project) }

          val result = projectService.save(project)

          result.userId shouldBe userId
          result.title shouldBe project.title
          result.startDate shouldBe project.startDate
          result.endDate shouldBe project.endDate
          result.thumbNailUrl shouldBe project.thumbNailUrl
          result.techStacks shouldBe project.techStacks
          result.recruitments shouldBe project.recruitments
          result.description shouldBe project.description
          result.isCompleted shouldBe project.isCompleted
          result.bookMarkers shouldBe project.bookMarkers
        }
      }
    }
  }
})

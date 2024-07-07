package pmeet.pmeetserver.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectFacadeServiceUnitTest : DescribeSpec({

//  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val projectService = mockk<ProjectService>(relaxed = true)

  lateinit var projectFacadeService: ProjectFacadeService

  lateinit var project: Project
  lateinit var userId: String
  lateinit var recruitments: List<Recruitment>

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectFacadeService = ProjectFacadeService(projectService)

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
    ReflectionTestUtils.setField(project, "id", "testId")
    ReflectionTestUtils.setField(project, "createdAt", LocalDateTime.of(2021, 5, 1, 0, 0, 0))
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("createProject") {
    context("createProjectRequestDto가 주어지면") {
      val requestDto = CreateProjectRequestDto(
        title = project.title,
        startDate = project.startDate,
        endDate = project.endDate,
        thumbNailUrl = project.thumbNailUrl,
        techStacks = project.techStacks,
        recruitments = project.recruitments.map { recruitment ->
          RecruitmentRequestDto(
            jobName = recruitment.jobName,
            numberOfRecruitment = recruitment.numberOfRecruitment
          )
        }.toList(),
        description = project.description
      )
      it("ProjectResponseDto를 반환한다") {
        runTest {
          coEvery { projectService.save(any()) } answers { project }
          val result = projectFacadeService.createProject(userId, requestDto)

          result.id shouldBe project.id
          result.title shouldBe requestDto.title
          result.startDate shouldBe requestDto.startDate
          result.endDate shouldBe requestDto.endDate
          result.thumbNailUrl shouldBe requestDto.thumbNailUrl
          result.techStacks shouldBe requestDto.techStacks
          result.recruitments.size shouldBe project.recruitments.size
          result.recruitments.forEachIndexed { index, recruitmentResponseDto ->
            recruitmentResponseDto.jobName shouldBe project.recruitments[index].jobName
            recruitmentResponseDto.numberOfRecruitment shouldBe project.recruitments[index].numberOfRecruitment
          }
          result.description shouldBe requestDto.description
          result.isCompleted shouldBe project.isCompleted
          result.bookMarkers shouldBe project.bookMarkers
          result.createdAt shouldBe project.createdAt
        }
      }
    }
  }
})

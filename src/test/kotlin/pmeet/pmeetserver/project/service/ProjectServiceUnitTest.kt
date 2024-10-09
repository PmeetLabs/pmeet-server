package pmeet.pmeetserver.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.enums.ProjectSortProperty
import pmeet.pmeetserver.project.repository.ProjectRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectServiceUnitTest : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

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
    ReflectionTestUtils.setField(project, "id", "testProjectId")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("프로젝트 객체가 주어지면") {
      it("저장 후 프로젝트를 반환한다") {
        runTest {
          every { projectRepository.save(any()) } answers { Mono.just(project) }

          val result = projectService.save(project)

          result.id shouldBe project.id
          result.userId shouldBe project.userId
          result.title shouldBe project.title
          result.startDate shouldBe project.startDate
          result.endDate shouldBe project.endDate
          result.thumbNailUrl shouldBe project.thumbNailUrl
          result.techStacks shouldBe project.techStacks
          result.recruitments shouldBe project.recruitments
          result.description shouldBe project.description
          result.isCompleted shouldBe project.isCompleted
          result.bookmarkers shouldBe project.bookmarkers
          result.createdAt shouldBe project.createdAt
          result.updatedAt shouldBe project.updatedAt
        }
      }
    }
  }

  describe("getProjectById") {
    context("존재하는 Project Id가 주어지면") {
      it("Project를 반환한다") {
        runTest {
          every { projectRepository.findById(project.id!!) } answers { Mono.just(project) }

          val result = projectService.getProjectById(project.id!!)

          result.id shouldBe project.id
          result.userId shouldBe project.userId
          result.title shouldBe project.title
          result.startDate shouldBe project.startDate
          result.endDate shouldBe project.endDate
          result.thumbNailUrl shouldBe project.thumbNailUrl
          result.techStacks shouldBe project.techStacks
          result.recruitments shouldBe project.recruitments
          result.description shouldBe project.description
          result.isCompleted shouldBe project.isCompleted
          result.bookmarkers shouldBe project.bookmarkers
          result.createdAt shouldBe project.createdAt
          result.updatedAt shouldBe project.updatedAt
        }
      }
    }
    context("존재하지 않는 Project Id가 주어지면") {
      it("NotFoundException을 던진다") {
        runTest {
          every { projectRepository.findById("nonExistProjectId") } answers { Mono.empty() }

          val exception = shouldThrow<EntityNotFoundException> {
            projectService.getProjectById("nonExistProjectId")
          }

          exception.errorCode shouldBe ErrorCode.PROJECT_NOT_FOUND
        }
      }
    }
  }

  describe("update") {
    context("업데이트 된 Project 객체가 주어지면") {
      val updatedProject = project.update(
        title = "updatedTitle",
        startDate = LocalDateTime.of(2022, 1, 1, 0, 0, 0),
        endDate = LocalDateTime.of(2022, 12, 31, 23, 59, 59),
        thumbNailUrl = "updatedThumbNailUrl",
        techStacks = listOf("updatedTechStack1", "updatedTechStack2"),
        recruitments = listOf(
          Recruitment(
            jobName = "updatedJobName",
            numberOfRecruitment = 3
          ),
          Recruitment(
            jobName = "updatedJobName2",
            numberOfRecruitment = 4
          )
        ),
        description = "updatedDescription"
      )
      it("저장 후 Project를 반환한다") {
        runTest {

          every { projectRepository.save(updatedProject) } answers { Mono.just(updatedProject) }

          val result = projectService.update(updatedProject)

          result.id shouldBe updatedProject.id
          result.userId shouldBe updatedProject.userId
          result.title shouldBe updatedProject.title
          result.startDate shouldBe updatedProject.startDate
          result.endDate shouldBe updatedProject.endDate
          result.thumbNailUrl shouldBe updatedProject.thumbNailUrl
          result.techStacks shouldBe updatedProject.techStacks
          result.recruitments shouldBe updatedProject.recruitments
          result.description shouldBe updatedProject.description
          result.isCompleted shouldBe updatedProject.isCompleted
          result.bookmarkers shouldBe updatedProject.bookmarkers
          result.updatedAt shouldBe updatedProject.updatedAt
          result.createdAt shouldBe updatedProject.createdAt
        }
      }
    }
  }

  describe("delete") {
    context("삭제하고자 하는 Project Id가 주어지면") {
      it("삭제한다") {
        runTest {
          every { projectRepository.delete(project) } answers { Mono.empty() }

          projectService.delete(project)

          verify(exactly = 1) { projectRepository.delete(project) }
        }
      }
    }
  }

  describe("searchSliceByFilter") {
    context("검색 조건이 주어지면") {
      it("Slice<Project>를 반환한다") {
        val pageNumber = 0
        val pageSize = 10
        val projects: MutableList<Project> = mutableListOf();
        for (i in 1..20) {
          val newProject = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
            thumbNailUrl = "testThumbNailUrl",
            techStacks = listOf("testTechStack1", "testTechStack2"),
            recruitments = recruitments,
            description = "testDescription"
          )
          projects.add(newProject)
        }
        runTest {
          every {
            projectRepository.findAllByFilter(
              any(),
              any(),
              any(),
              any(),
              any(),
              any()
            )
          } answers { Flux.fromIterable(projects.subList(0, pageSize + 1)) }

          val result = projectService.searchSliceByFilter(
            isCompleted = false,
            filterType = null,
            filterValue = null,
            userId,
            false,
            pageable = PageRequest.of(
              pageNumber,
              pageSize,
              Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.property)
            )
          )

          result.size shouldBe pageSize
          result.content shouldBe projects.subList(0, pageSize)
          result.isLast shouldBe false
          result.isFirst shouldBe true
        }
      }
    }
  }

  describe("getProjectSliceByUserIdOrderByCreatedAtDesc") {
    context("유저 ID와 페이지 정보가 주어지면") {
      it("Slice<Project>를 반환한다") {
        val pageNumber = 0
        val pageSize = 10
        val projects: MutableList<Project> = mutableListOf();
        for (i in 1..20) {
          val newProject = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
            thumbNailUrl = "testThumbNailUrl",
            techStacks = listOf("testTechStack1", "testTechStack2"),
            recruitments = recruitments,
            description = "testDescription"
          )
          ReflectionTestUtils.setField(newProject, "createdAt", LocalDateTime.of(2024, 8, i, 0, 0, 0))
          projects.add(newProject)
        }
        runTest {
          every {
            projectRepository.findProjectByUserIdOrderByCreatedAtDesc(
              any(),
              any()
            )
          } answers { Flux.fromIterable(projects.subList(0, pageSize + 1)) }

          val result = projectService.getProjectSliceByUserIdOrderByCreatedAtDesc(
            userId = userId,
            pageable = PageRequest.of(
              pageNumber,
              pageSize
            )
          )

          result.size shouldBe pageSize
          result.content shouldBe projects.subList(0, pageSize)
          result.content.first().id shouldBe projects[0].id
          result.content.last().id shouldBe projects[pageSize - 1].id
          result.isFirst shouldBe true
          result.isLast shouldBe false
        }
      }
    }
  }
})

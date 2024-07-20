package pmeet.pmeetserver.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.comment.CreateProjectCommentRequestDto

@ExperimentalCoroutinesApi
internal class ProjectFacadeServiceUnitTest : DescribeSpec({

//  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val projectService = mockk<ProjectService>(relaxed = true)
  val projectCommentService = mockk<ProjectCommentService>(relaxed = true)

  lateinit var projectFacadeService: ProjectFacadeService

  lateinit var project: Project
  lateinit var projectComment: ProjectComment
  lateinit var userId: String
  lateinit var forbiddenUserId: String
  lateinit var recruitments: List<Recruitment>

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectFacadeService = ProjectFacadeService(projectService, projectCommentService)

    userId = "testUserId"
    forbiddenUserId = "forbiddenUserId"

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

    projectComment = ProjectComment(
      projectId = project.id!!,
      userId = userId,
      content = "testContent",
      isDeleted = false,
    )
    ReflectionTestUtils.setField(projectComment, "id", "testCommentId")
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

  describe("updateProject") {
    val requestDto = UpdateProjectRequestDto(
      id = project.id!!,
      title = "updateTitle",
      startDate = LocalDateTime.of(2024, 7, 20, 0, 0, 0),
      endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
      thumbNailUrl = "updateThumbNailUrl",
      techStacks = listOf("updateTechStack1", "updateTechStack2"),
      recruitments = listOf(
        RecruitmentRequestDto(
          jobName = "updateJobName1",
          numberOfRecruitment = 3
        ),
        RecruitmentRequestDto(
          jobName = "updateJobName2",
          numberOfRecruitment = 4
        )
      ),
      description = "updateDescription"
    )
    context("Project의 Userid와 요청으로 들어온 userId가 같은 경우") {
      it("업데이트 후 ProjectResponseDto를 반환한다") {
        runTest {
          coEvery { projectService.getProjectById(project.id!!) } answers { project }
          coEvery { projectService.update(any()) } answers { project }

          val result = projectFacadeService.updateProject(userId, requestDto)

          result.id shouldBe project.id
          result.title shouldBe requestDto.title
          result.startDate shouldBe requestDto.startDate
          result.endDate shouldBe requestDto.endDate
          result.thumbNailUrl shouldBe requestDto.thumbNailUrl
          result.techStacks shouldBe requestDto.techStacks
          result.recruitments.size shouldBe requestDto.recruitments.size
          result.recruitments.forEachIndexed { index, recruitmentResponseDto ->
            recruitmentResponseDto.jobName shouldBe requestDto.recruitments[index].jobName
            recruitmentResponseDto.numberOfRecruitment shouldBe requestDto.recruitments[index].numberOfRecruitment
          }
          result.description shouldBe requestDto.description
          result.isCompleted shouldBe project.isCompleted
          result.bookMarkers shouldBe project.bookMarkers
          result.createdAt shouldBe project.createdAt
        }
      }
    }
    context("Project Userid와 요청으로 들어온 userId가 다른 경우") {
      it("ForBiddenRequestException을 던진다") {
        runTest {
          coEvery { projectService.update(any()) } answers { project }

          val exception = shouldThrow<ForbiddenRequestException> {
            projectFacadeService.updateProject(forbiddenUserId, requestDto)
          }

          exception.errorCode shouldBe ErrorCode.PROJECT_UPDATE_FORBIDDEN
        }
      }
    }
  }

  describe("createProjectComment") {
    context("createProjectCommentRequestDto가 주어지면") {
      val requestDto = CreateProjectCommentRequestDto(
        projectId = projectComment.projectId,
        parentCommentId = projectComment.parentCommentId,
        content = projectComment.content
      )
      it("ProjectCommentResponseDto를 반환한다") {
        runTest {
          coEvery { projectService.getProjectById(requestDto.projectId) } answers { project }
          coEvery { projectCommentService.save(any()) } answers { projectComment }

          val result = projectFacadeService.createProjectComment(userId, requestDto)

          result.id shouldBe projectComment.id
          result.parentCommentId shouldBe projectComment.parentCommentId
          result.content shouldBe projectComment.content
          result.userId shouldBe projectComment.userId
          result.projectId shouldBe projectComment.projectId
          result.isDeleted shouldBe projectComment.isDeleted
        }
      }
    }
  }

  describe("deleteProjectComment") {
    context("commentId가 주어지면") {
      val commentId = projectComment.id!!
      it("ProjectCommentResponseDto를 반환한다") {
        runTest {
          coEvery { projectCommentService.getProjectCommentById(commentId) } answers { projectComment }
          coEvery { projectCommentService.save(any()) } answers { projectComment }

          val result = projectFacadeService.deleteProjectComment(userId, commentId)

          result.id shouldBe projectComment.id
          result.parentCommentId shouldBe projectComment.parentCommentId
          result.content shouldBe "작성자가 삭제한 댓글입니다."
          result.userId shouldBe projectComment.userId
          result.projectId shouldBe projectComment.projectId
          result.isDeleted shouldBe projectComment.isDeleted
        }
      }

      it("권한이 없는 userId의 경우 ForbiddenRequestException 반환한다") {
        runTest {
          coEvery { projectCommentService.getProjectCommentById(commentId) } answers { projectComment }

          shouldThrow<ForbiddenRequestException> {
            projectFacadeService.deleteProjectComment(forbiddenUserId, commentId)
          }.errorCode shouldBe ErrorCode.PROJECT_COMMENT_DELETE_FORBIDDEN
        }
      }
    }
  }
})

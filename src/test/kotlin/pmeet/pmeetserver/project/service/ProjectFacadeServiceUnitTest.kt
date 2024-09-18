package pmeet.pmeetserver.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.data.domain.Sort
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.file.service.FileService
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectBookmark
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.dto.comment.ProjectCommentWithChildDto
import pmeet.pmeetserver.project.dto.comment.request.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.comment.response.ProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.RecruitmentRequestDto
import pmeet.pmeetserver.project.dto.request.SearchProjectRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.tryout.request.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.request.PatchProjectTryoutRequestDto
import pmeet.pmeetserver.project.enums.ProjectSortProperty
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.service.UserService
import pmeet.pmeetserver.user.service.notification.NotificationService
import pmeet.pmeetserver.user.service.resume.ResumeService
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectFacadeServiceUnitTest : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val projectService = mockk<ProjectService>(relaxed = true)
  val projectCommentService = mockk<ProjectCommentService>(relaxed = true)
  val resumeService = mockk<ResumeService>(relaxed = true)
  val projectTryoutService = mockk<ProjectTryoutService>(relaxed = true)
  val projectMemberService = mockk<ProjectMemberService>(relaxed = true)
  val userService = mockk<UserService>(relaxed = true)
  val fileService = mockk<FileService>(relaxed = true)
  val notificationService = mockk<NotificationService>(relaxed = true)

  lateinit var projectFacadeService: ProjectFacadeService

  lateinit var project: Project
  lateinit var projectComment: ProjectComment
  lateinit var userId: String
  lateinit var forbiddenUserId: String
  lateinit var recruitments: List<Recruitment>
  lateinit var resume: Resume
  lateinit var projectTryout: ProjectTryout
  lateinit var user: User

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    projectFacadeService = ProjectFacadeService(
      projectService,
      projectCommentService,
      resumeService,
      projectTryoutService,
      projectMemberService,
      userService,
      fileService,
      notificationService
    )

    userId = "testUserId"
    forbiddenUserId = "forbiddenUserId"

    user = User(
      id = userId,
      email = "testEmail@test.com",
      name = "testName",
      nickname = "nickname",
      phoneNumber = "phone",
      gender = Gender.MALE,
      profileImageUrl = "image-url"
    )

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
    ReflectionTestUtils.setField(project, "updatedAt", LocalDateTime.of(2021, 6, 1, 0, 0, 0))
    projectComment = ProjectComment(
      projectId = project.id!!,
      userId = userId,
      content = "testContent",
      isDeleted = false,
    )
    ReflectionTestUtils.setField(projectComment, "id", "testCommentId")

    resume = generateResume()

    projectTryout = ProjectTryout(
      projectId = "testProjectId",
      userId = userId,
      resumeId = "resumeId",
      userName = "testUserName",
      userSelfDescription = "userSelfDescription",
      positionName = "testPosition",
      tryoutStatus = ProjectTryoutStatus.INREVIEW,
      createdAt = LocalDateTime.now()
    )
    ReflectionTestUtils.setField(projectTryout, "id", "testTryoutId")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("getProjectByProjectId") {
    context("projectId가 주어지면") {
      it("ProjectWithUserResponseDto를 반환한다") {
        runTest {
          val thumbNailDownloadUrl = "testThumbNailDownloadUrl"
          val profileImageDownloadUrl = "testProfileImageDownloadUrl"
          coEvery { projectService.getProjectById(any()) } answers { project }
          coEvery { userService.getUserById(project.userId) } answers { user }
          coEvery { fileService.generatePreSignedUrlToDownload(project.thumbNailUrl!!) } answers { thumbNailDownloadUrl }
          coEvery { fileService.generatePreSignedUrlToDownload(user.profileImageUrl!!) } answers { profileImageDownloadUrl }

          val result = projectFacadeService.getProjectByProjectId(user.id!!, project.id!!)

          result.id shouldBe project.id
          result.userId shouldBe project.userId
          result.title shouldBe project.title
          result.startDate shouldBe project.startDate
          result.endDate shouldBe project.endDate
          result.thumbNailUrl shouldBe thumbNailDownloadUrl
          result.description shouldBe project.description
          result.isCompleted shouldBe project.isCompleted
          result.userInfo.id shouldBe user.id
          result.userInfo.profileImageUrl shouldBe profileImageDownloadUrl
          result.techStacks shouldBe project.techStacks
          result.isMyBookmark shouldBe false
        }
      }
    }
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
          val thumbNailDownloadUrl = "testThumbNailDownloadUrl"
          coEvery { projectService.save(any()) } answers { project }
          coEvery { fileService.generatePreSignedUrlToDownload(project.thumbNailUrl!!) } answers { thumbNailDownloadUrl }

          val result = projectFacadeService.createProject(userId, requestDto)

          result.id shouldBe project.id
          result.title shouldBe requestDto.title
          result.startDate shouldBe requestDto.startDate
          result.endDate shouldBe requestDto.endDate
          result.thumbNailUrl shouldBe thumbNailDownloadUrl
          result.techStacks shouldBe requestDto.techStacks
          result.recruitments.size shouldBe project.recruitments.size
          result.recruitments.forEachIndexed { index, recruitmentResponseDto ->
            recruitmentResponseDto.jobName shouldBe project.recruitments[index].jobName
            recruitmentResponseDto.numberOfRecruitment shouldBe project.recruitments[index].numberOfRecruitment
          }
          result.description shouldBe requestDto.description
          result.isCompleted shouldBe project.isCompleted
          result.bookmarked shouldBe false
          result.createdAt shouldBe project.createdAt
          result.updatedAt shouldBe project.updatedAt
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
    coEvery { projectService.getProjectById(project.id!!) } answers { project }
    context("Project의 userId와 요청으로 들어온 userId가 같은 경우") {
      it("업데이트 후 ProjectResponseDto를 반환한다") {
        runTest {
          val thumbNailDownloadUrl = "testThumbNailDownloadUrl"
          coEvery { projectService.update(any()) } answers { project }
          coEvery { fileService.generatePreSignedUrlToDownload(requestDto.thumbNailUrl!!) } answers { thumbNailDownloadUrl }

          val result = projectFacadeService.updateProject(project.userId, requestDto)

          result.id shouldBe project.id
          result.title shouldBe requestDto.title
          result.startDate shouldBe requestDto.startDate
          result.endDate shouldBe requestDto.endDate
          result.thumbNailUrl shouldBe thumbNailDownloadUrl
          result.techStacks shouldBe requestDto.techStacks
          result.recruitments.size shouldBe requestDto.recruitments.size
          result.recruitments.forEachIndexed { index, recruitmentResponseDto ->
            recruitmentResponseDto.jobName shouldBe requestDto.recruitments[index].jobName
            recruitmentResponseDto.numberOfRecruitment shouldBe requestDto.recruitments[index].numberOfRecruitment
          }
          result.description shouldBe requestDto.description
          result.isCompleted shouldBe project.isCompleted
          result.bookmarked shouldBe false
          result.createdAt shouldBe project.createdAt
          result.updatedAt shouldBe project.updatedAt
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

  describe("deleteProject") {
    val projectId = project.id!!
    coEvery { projectService.getProjectById(projectId) } answers { project }
    context("Project의 userId와 요청으로 들어온 userId가 같은 경우") {
      it("Project를 삭제한다") {
        runTest {
          coEvery { projectService.delete(project) } answers { Unit }

          projectFacadeService.deleteProject(project.userId, projectId)

          coVerify(exactly = 1) { projectService.delete(project) }
        }
      }

      it("ProjectComment를 삭제한다") {
        runTest {
          coEvery { projectCommentService.deleteAllByProjectId(projectId) } answers { Unit }

          projectFacadeService.deleteProject(project.userId, projectId)

          coVerify(exactly = 1) { projectCommentService.deleteAllByProjectId(projectId) }
        }
      }

      it("ProjectTryout을 삭제한다") {
        runTest {
          coEvery { projectTryoutService.deleteAllByProjectId(projectId) } answers { Unit }

          projectFacadeService.deleteProject(project.userId, projectId)

          coVerify(exactly = 1) { projectTryoutService.deleteAllByProjectId(projectId) }
        }
      }
    }
    context("Project Userid와 요청으로 들어온 userId가 다른 경우") {
      it("ForBiddenRequestException을 던진다") {
        runTest {
          val exception = shouldThrow<ForbiddenRequestException> {
            projectFacadeService.deleteProject("anotherUserId", projectId)
          }

          exception.errorCode shouldBe ErrorCode.PROJECT_DELETE_FORBIDDEN
        }
      }
    }
  }

  describe("createProjectTryout") {
    context("createProjectTryoutRequestDto 주어지면") {
      val requestDto = CreateProjectTryoutRequestDto(
        projectId = "testProjectId",
        resumeId = resume.id!!,
        positionName = "positionName",
      )

      it("ProjectTryoutResponseDto 반환한다") {
        runTest {
          coEvery { resumeService.getByResumeId(any()) } answers { resume }
          coEvery { projectTryoutService.save(any()) } answers { projectTryout }

          val result = projectFacadeService.createProjectTryout(resume.userId, requestDto)

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

  describe("getProjectTryoutListByProjectId") {
    context("userId 와 projectId 가 주어지면") {
      val projectId = "testProjectId"

      it("ProjectTryoutResponseDto list 를 반환한다") {
        runTest {
          coEvery { projectService.getProjectById(any()) } answers { project }
          coEvery { projectTryoutService.findAllByProjectId(any()) } answers { mutableListOf(projectTryout) }

          val result = projectFacadeService.getProjectTryoutListByProjectId(userId, projectId)

          result.get(0).id shouldBe projectTryout.id
          result.get(0).resumeId shouldBe projectTryout.resumeId
          result.get(0).userId shouldBe projectTryout.userId
          result.get(0).userName shouldBe projectTryout.userName
          result.get(0).userSelfDescription shouldBe projectTryout.userSelfDescription
          result.get(0).positionName shouldBe projectTryout.positionName
          result.get(0).projectId shouldBe projectTryout.projectId
        }
      }

      it("해당 project 를 조회할 권한이 없다면 PROJECT_TRYOUT_VIEW_FORBIDDEN 예외를 반환한다") {
        runTest {
          coEvery { projectService.getProjectById(any()) } answers { project }
          coEvery { projectTryoutService.findAllByProjectId(any()) } answers { mutableListOf(projectTryout) }

          val exception = shouldThrow<ForbiddenRequestException> {
            projectFacadeService.getProjectTryoutListByProjectId("unvalidUserId", projectId)
          }

          exception.errorCode shouldBe ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN
        }
      }
    }
  }

  describe("getProjectCommentList") {
    val projectId = project.id!!
    val responseDto = listOf(
      ProjectCommentWithChildDto(
        id = "testCommentId",
        parentCommentId = null,
        projectId = "testProjectId",
        userId = userId,
        content = "testContent",
        likerIdList = listOf(),
        createdAt = LocalDateTime.of(2024, 7, 16, 0, 0, 0),
        isDeleted = false,
        childComments = listOf(
          ProjectCommentResponseDto(
            id = "childCommentId",
            parentCommentId = "testCommentId",
            projectId = "testProjectId",
            userId = userId,
            content = "testContent",
            likerIdList = listOf(),
            createdAt = LocalDateTime.of(2024, 7, 16, 0, 0, 0),
            isDeleted = false,
          )
        )
      )
    )

    coEvery { projectCommentService.getProjectCommentWithChildByProjectId(projectId) } answers { responseDto }
    coEvery { fileService.generatePreSignedUrlToDownload(any()) } answers { "test" }

    context("projectId를 입력받으면") {
      it("ProjectCommentWithChildResponseDto를 조회한다.") {
        runTest {
          val result = projectFacadeService.getProjectCommentList(projectId)

          result[0].id shouldBe responseDto[0].id
          result[0].parentCommentId shouldBe responseDto[0].parentCommentId
          result[0].projectId shouldBe responseDto[0].projectId
          result[0].userId shouldBe responseDto[0].userId
          result[0].content shouldBe responseDto[0].content
          result[0].isDeleted shouldBe responseDto[0].isDeleted

          result[0].childComments[0].id shouldBe responseDto[0].childComments[0].id
          result[0].childComments[0].content shouldBe responseDto[0].childComments[0].content
          result[0].childComments[0].userId shouldBe responseDto[0].childComments[0].userId
          result[0].childComments[0].parentCommentId shouldBe responseDto[0].childComments[0].parentCommentId
          result[0].childComments[0].projectId shouldBe responseDto[0].childComments[0].projectId
          result[0].childComments[0].isDeleted shouldBe responseDto[0].childComments[0].isDeleted
        }
      }
    }
  }

  describe("searchProjectSlice") {
    context("userId와 SearchProjectRequestDto가 주어지면") {
      val pageNumber = 0
      val pageSize = 10
      val requestDto = SearchProjectRequestDto.of(
        isCompleted = true,
        filterType = null,
        filterValue = null,
        page = pageNumber,
        size = pageSize,
        sortBy = ProjectSortProperty.BOOK_MARKERS,
        direction = Sort.Direction.ASC
      )
      it("Slice<SearchProjectResponseDto>를 반환한다") {
        val requesterUserId = "requesterId"
        val projects: MutableList<Project> = mutableListOf();
        for (i in 1..20) {
          val newProject = Project(
            id = "testId$i",
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2021, 1, 1, 0, 0, 0),
            endDate = LocalDateTime.of(2021, 12, 31, 23, 59, 59),
            thumbNailUrl = "testThumbNailUrl",
            techStacks = listOf("testTechStack1", "testTechStack2"),
            recruitments = recruitments,
            description = "testDescription"
          )
          if (i == 1) {
            newProject.bookmarkers.add(ProjectBookmark(requesterUserId, LocalDateTime.now()))
          }
          for (j in 1..i) {
            newProject.bookmarkers.add(ProjectBookmark("$userId$i", LocalDateTime.now()))
          }
          projects.add(newProject)
        }
        runTest {
          coEvery {
            projectService.searchSliceByFilter(
              any(),
              any(),
              any(),
              any()
            )
          } answers { SliceImpl(projects.subList(0, pageSize), requestDto.pageable, true) }

          val result = projectFacadeService.searchProjectSlice(requesterUserId, requestDto)

          result.size shouldBe pageSize
          result.isFirst shouldBe true
          result.isLast shouldBe false
          result.content.first().bookmarked shouldBe true
          result.content.last().bookmarked shouldBe false
        }
      }
    }
  }

  describe("addBookmark") {
    context("userId와 projectId가 주어지면") {
      val projectId = project.id!!
      it("북마크를 추가한다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery { projectService.update(any()) } answers { project }

          projectFacadeService.addBookmark(userId, projectId)

          project.bookmarkers.size shouldBe 1
          project.bookmarkers[0].userId shouldBe userId
          project.bookmarkers[0].addedAt shouldNotBe null
        }
      }

      it("이미 북마크를 추가한 경우 대치 한다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery { projectService.update(any()) } answers { project }

          val localDateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0)
          project.bookmarkers.add(ProjectBookmark(userId, localDateTime))
          projectFacadeService.addBookmark(userId, projectId)

          project.bookmarkers.size shouldBe 1
          project.bookmarkers[0].userId shouldBe userId
          project.bookmarkers[0].addedAt shouldNotBe localDateTime
        }
      }
    }
  }

  describe("deleteBookmark") {
    context("userId와 projectId가 주어지면") {
      val projectId = project.id!!
      it("북마크를 삭제한다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery { projectService.update(any()) } answers { project }

          project.bookmarkers.add(ProjectBookmark(userId, LocalDateTime.now()))
          projectFacadeService.deleteBookmark(userId, projectId)

          project.bookmarkers.size shouldBe 0
        }
      }

      it("북마크가 없는 경우 아무것도 하지 않는다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery { projectService.update(any()) } answers { project }

          projectFacadeService.deleteBookmark(userId, projectId)

          project.bookmarkers.size shouldBe 0
        }
      }
    }
  }

  describe("patchProjectTryoutStatusToAccept") {
    context("userId와 patchRequest 가 주어지면") {
      val projectId = project.id!!
      val tryoutId = projectTryout.id!!
      val requestDto = PatchProjectTryoutRequestDto(projectId, tryoutId)
      it("프로젝트 지원 상태를 합격으로 업데이트한다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery {
            projectTryoutService.updateTryoutStatus(
              tryoutId,
              ProjectTryoutStatus.ACCEPTED
            )
          } answers { projectTryout }
          coEvery { projectMemberService.save(any()) } answers { projectTryout.createProjectMember() }

          projectFacadeService.patchProjectTryoutStatusToAccept(userId, requestDto)

          coVerify(exactly = 1) { projectTryoutService.updateTryoutStatus(tryoutId, ProjectTryoutStatus.ACCEPTED) }
        }
      }
    }
  }

  describe("pathProjectTryoutStatusToReject") {
    context("userId와 patchRequest 가 주어지면") {
      val projectId = project.id!!
      val tryoutId = projectTryout.id!!
      val requestDto = PatchProjectTryoutRequestDto(projectId, tryoutId)
      it("프로젝트 지원 상태를 불합격으로 업데이트한다") {
        runTest {
          coEvery { projectService.getProjectById(projectId) } answers { project }
          coEvery {
            projectTryoutService.updateTryoutStatus(
              tryoutId,
              ProjectTryoutStatus.REJECTED
            )
          } answers { projectTryout }
          coEvery { projectMemberService.save(any()) } answers { projectTryout.createProjectMember() }

          projectFacadeService.pathProjectTryoutStatusToReject(userId, requestDto)

          coVerify(exactly = 1) { projectTryoutService.updateTryoutStatus(tryoutId, ProjectTryoutStatus.REJECTED) }
        }
      }
    }
  }

  describe("getMyProjectSlice") {
    context("userId와 pageable이 주어지면") {
      val pageNumber = 0
      val pageSize = 10
      val pageable = PageRequest.of(pageNumber, pageSize)
      it("Slice<GetMyProjectResponseDto>를 반환한다") {
        runTest {
          val projects: MutableList<Project> = mutableListOf();
          val downloadUrls = mutableListOf<String>()
          for (i in 1..20) {
            val newProject = Project(
              id = "testId$i",
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
            downloadUrls.add("testThumbNailDownloadUrl%i")
          }
          coEvery {
            projectService.getProjectSliceByUserIdOrderByCreatedAtDesc(
              userId,
              pageable
            )
          } answers { SliceImpl(projects.subList(0, pageable.pageSize), pageable, true) }
          coEvery { fileService.generatePreSignedUrlToDownload(any()) } answers { downloadUrls.iterator().next() }

          val result = projectFacadeService.getMyProjectSlice(userId, pageable)

          result.size shouldBe pageable.pageSize
          result.content.forEachIndexed { index, getMyProjectResponseDto ->
            getMyProjectResponseDto.id shouldBe projects[index].id
            getMyProjectResponseDto.title shouldBe projects[index].title
            getMyProjectResponseDto.startDate shouldBe projects[index].startDate
            getMyProjectResponseDto.thumbNailUrl shouldBe downloadUrls[index]
            getMyProjectResponseDto.description shouldBe projects[index].description
            getMyProjectResponseDto.isCompleted shouldBe projects[index].isCompleted
            getMyProjectResponseDto.createdAt shouldBe projects[index].createdAt
          }
          result.isFirst shouldBe true
          result.isLast shouldBe false
        }
      }
    }
  }

  describe("getAcceptedProjectTryoutListByProjectId") {
    val projectId = "testProjectId"
    val requestedUserId = "testUserId"

    val acceptedTryout1 = ProjectTryout(
      id = "testTryoutId1",
      projectId = projectId,
      userId = "testUserId2",
      resumeId = "resumeId2",
      userName = "testUserName2",
      userSelfDescription = "userSelfDescription2",
      positionName = "testPosition2",
      tryoutStatus = ProjectTryoutStatus.ACCEPTED,
      createdAt = LocalDateTime.of(2024, 7, 24, 0, 0, 0)
    )

    val acceptedTryout2 = ProjectTryout(
      id = "testTryoutId2",
      projectId = projectId,
      userId = "testUserId2",
      resumeId = "resumeId2",
      userName = "testUserName2",
      userSelfDescription = "userSelfDescription2",
      positionName = "testPosition2",
      tryoutStatus = ProjectTryoutStatus.ACCEPTED,
      createdAt = LocalDateTime.of(2024, 7, 24, 0, 0, 0)
    )

    context("프로젝트 생성자가 요청한 경우") {
      it("승인된 지원 목록을 반환한다") {
        runTest {
          val acceptedTryouts = listOf(acceptedTryout1, acceptedTryout2)

          coEvery { projectService.getProjectById(projectId) } returns project
          coEvery { projectTryoutService.findAllAcceptedTryoutByProjectId(projectId) } returns acceptedTryouts

          val result = projectFacadeService.getAcceptedProjectTryoutListByProjectId(requestedUserId, projectId)

          result.size shouldBe 2
          result[0].id shouldBe acceptedTryout1.id
          result[0].userName shouldBe acceptedTryout1.userName
          result[0].tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED
          result[1].id shouldBe acceptedTryout2.id
          result[1].userName shouldBe acceptedTryout2.userName
          result[1].tryoutStatus shouldBe ProjectTryoutStatus.ACCEPTED

          coVerify(exactly = 1) { projectService.getProjectById(projectId) }
          coVerify(exactly = 1) { projectTryoutService.findAllAcceptedTryoutByProjectId(projectId) }
        }
      }
    }

    context("프로젝트 생성자가 아닌 사용자가 요청한 경우") {
      it("ForbiddenRequestException을 던진다") {
        runTest {
          val unauthorizedUserId = "unauthorizedUserId"

          coEvery { projectService.getProjectById(projectId) } returns project

          val exception = shouldThrow<ForbiddenRequestException> {
            projectFacadeService.getAcceptedProjectTryoutListByProjectId(unauthorizedUserId, projectId)
          }

          exception.errorCode shouldBe ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN

          coVerify(exactly = 1) { projectService.getProjectById(projectId) }
          coVerify(exactly = 0) { projectTryoutService.findAllAcceptedTryoutByProjectId(any()) }
        }
      }
    }
  }
})

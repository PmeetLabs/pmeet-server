package pmeet.pmeetserver.project.service

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.file.service.FileService
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.domain.ProjectMember
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.dto.comment.request.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.comment.response.GetProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.comment.response.GetProjectCommentWithChildResponseDto
import pmeet.pmeetserver.project.dto.comment.response.ProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.request.CompleteProjectRequestDto
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.SearchProjectRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.response.CompletedProjectResponseDto
import pmeet.pmeetserver.project.dto.response.GetMyInProgressProjectResponseDto
import pmeet.pmeetserver.project.dto.response.GetMyInReviewProjectResponseDto
import pmeet.pmeetserver.project.dto.response.GetMyProjectResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectMemberInfoDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectWithUserResponseDto
import pmeet.pmeetserver.project.dto.response.SearchCompleteProjectResponseDto
import pmeet.pmeetserver.project.dto.response.SearchProjectResponseDto
import pmeet.pmeetserver.project.dto.tryout.request.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.request.PatchProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.response.ProjectTryoutResponseDto
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.service.UserService
import pmeet.pmeetserver.user.service.notification.NotificationService
import pmeet.pmeetserver.user.service.resume.ResumeService
import java.time.LocalDateTime

@Service
class ProjectFacadeService(
  private val projectService: ProjectService,
  private val projectCommentService: ProjectCommentService,
  private val resumeService: ResumeService,
  private val projectTryoutService: ProjectTryoutService,
  private val projectMemberService: ProjectMemberService,
  private val userService: UserService,
  private val fileService: FileService,
  private val notificationService: NotificationService
) {

  @Transactional
  suspend fun createProject(userId: String, requestDto: CreateProjectRequestDto): ProjectResponseDto {
    val recruitments =
      requestDto
        .recruitments
        .map { Recruitment(it.jobName, it.numberOfRecruitment) }
        .toList()

    var project = Project(
      userId = userId,
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      recruitments = recruitments,
      description = requestDto.description
    )

    val user = userService.getUserById(userId)
    project = projectService.save(project)

    val projectMember = ProjectMember(
      userId = userId,
      userName = user.name,
      userThumbnail = user.profileImageUrl,
      projectId = project.id!!,
      createdAt = LocalDateTime.now()
    )
    projectMemberService.save(projectMember)

    return ProjectResponseDto.of(
      project,
      userId,
      project.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional
  suspend fun updateProject(userId: String, requestDto: UpdateProjectRequestDto): ProjectResponseDto {
    val originalProject = checkUserHasAuthToProject(requestDto.id, userId, ErrorCode.PROJECT_UPDATE_FORBIDDEN)

    originalProject.update(
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      recruitments = requestDto.recruitments.map { Recruitment(it.jobName, it.numberOfRecruitment) },
      description = requestDto.description
    )

    return ProjectResponseDto.of(
      projectService.update(originalProject),
      userId,
      originalProject.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional
  suspend fun createProjectComment(userId: String, requestDto: CreateProjectCommentRequestDto):
    ProjectCommentResponseDto {

    val project = projectService.getProjectById(requestDto.projectId)

    // parentId에 해당하는 댓글이 있는지 검증
    requestDto.parentCommentId?.let { parentId ->
      projectCommentService.getProjectCommentById(parentId)
    }

    val projectComment = ProjectComment(
      parentCommentId = requestDto.parentCommentId,
      projectId = project.id!!,
      userId = userId,
      content = requestDto.content
    )

    val savedProjectComment = projectCommentService.save(projectComment)
    notificationService.createNotification(NotificationType.COMMENT, project.userId)

    return ProjectCommentResponseDto.from(savedProjectComment)
  }

  @Transactional
  suspend fun deleteProjectComment(userId: String, commentId: String): ProjectCommentResponseDto {

    val comment = projectCommentService.getProjectCommentById(commentId)

    if (comment.userId != userId) {
      throw ForbiddenRequestException(ErrorCode.PROJECT_COMMENT_DELETE_FORBIDDEN)
    }

    comment.delete()

    return ProjectCommentResponseDto.from(projectCommentService.save(comment))
  }

  @Transactional
  suspend fun deleteProject(usedId: String, projectId: String) {
    val project = checkUserHasAuthToProject(projectId, usedId, ErrorCode.PROJECT_DELETE_FORBIDDEN)

    projectCommentService.deleteAllByProjectId(projectId)
    projectTryoutService.deleteAllByProjectId(projectId)
    projectService.delete(project)
  }

  @Transactional
  suspend fun createProjectTryout(
    userId: String,
    requestDto: CreateProjectTryoutRequestDto
  ): ProjectTryoutResponseDto {
    val resume = resumeService.getByResumeId(requestDto.resumeId)

    if (resume.userId != userId) {
      throw ForbiddenRequestException(ErrorCode.RESUME_TRYOUT_FORBIDDEN)
    }

    val projectTryout = ProjectTryout(
      resumeId = requestDto.resumeId,
      userId = userId,
      userName = resume.userName,
      userSelfDescription = resume.selfDescription.orEmpty(),
      positionName = requestDto.positionName,
      tryoutStatus = ProjectTryoutStatus.INREVIEW,
      projectId = requestDto.projectId,
      createdAt = LocalDateTime.now()
    )

    val savedProjectTryout = projectTryoutService.save(projectTryout)

    val project = projectService.getProjectById(requestDto.projectId)
    notificationService.createNotification(NotificationType.APPLY, project.userId)

    return ProjectTryoutResponseDto.from(savedProjectTryout)
  }

  @Transactional(readOnly = true)
  suspend fun getProjectTryoutListByProjectId(userId: String, projectId: String): List<ProjectTryoutResponseDto> {
    checkUserHasAuthToProject(projectId, userId, ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN)
    val projectTryoutList = projectTryoutService.findAllByProjectId(projectId)
    return projectTryoutList.map { ProjectTryoutResponseDto.from(it) }
  }

  @Transactional(readOnly = true)
  suspend fun getAcceptedProjectTryoutListByProjectId(
    requestedUserId: String,
    projectId: String
  ): List<ProjectTryoutResponseDto> {
    checkUserHasAuthToProject(projectId, requestedUserId, ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN)
    val projectTryoutList = projectTryoutService.findAllAcceptedTryoutByProjectId(projectId)
    return projectTryoutList.map { ProjectTryoutResponseDto.from(it) }
  }

  @Transactional(readOnly = true)
  suspend fun getProjectCommentList(projectId: String): List<GetProjectCommentWithChildResponseDto> {
    val projectCommentWithChild = projectCommentService.getProjectCommentWithChildByProjectId(projectId)

    return projectCommentWithChild.map { parentComment ->
      val user = parentComment.userId.let { userService.getUserById(it) }
      GetProjectCommentWithChildResponseDto.from(
        parentComment,
        user,
        user.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
        parentComment.childComments.map { childComment ->
          val childUser = childComment.userId.let { userService.getUserById(it) }
          GetProjectCommentResponseDto.from(
            childComment,
            childUser,
            childUser.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) })
        }
      )
    }
  }

  @Transactional(readOnly = true)
  suspend fun searchProjectSlice(userId: String, requestDto: SearchProjectRequestDto): Slice<SearchProjectResponseDto> {
    val projects = projectService.searchSliceByFilter(
      requestDto.isCompleted,
      requestDto.filterType,
      requestDto.filterValue,
      userId,
      false,
      requestDto.pageable
    )
    return SliceImpl(
      projects.content.map {
        SearchProjectResponseDto.of(
          it,
          userId,
          it.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
        )
      },
      projects.pageable,
      projects.hasNext()
    )
  }

  /**
   * 완료 프밋을 목록 조회
   */
  @Transactional
  suspend fun searchCompleteProjectSlice(
    userId: String,
    requestDto: SearchProjectRequestDto,
    isMy: Boolean?
  ): Slice<SearchCompleteProjectResponseDto> {
    val projects = projectService.searchSliceByFilter(
      requestDto.isCompleted,
      requestDto.filterType,
      requestDto.filterValue,
      userId,
      isMy ?: false,
      requestDto.pageable,
    )
    val projectMemberList =
      projectMemberService.findAllMembersByProjectId(projects.content.mapTo(mutableSetOf()) { it.id!! });
    val memberThumbnailMap = projectMemberList.filter { !it.userThumbnail.isNullOrEmpty() }.associate { projectMember ->
      val id = projectMember.id ?: throw IllegalStateException("ProjectMember id cannot be null")
      val thumbnailUrl = fileService.generatePreSignedUrlToDownload(projectMember.userThumbnail!!)
      id to thumbnailUrl
    }
    return SliceImpl(
      projects.content.map {
        SearchCompleteProjectResponseDto.of(
          it,
          userId,
          projectMemberList.filter { member -> member.projectId == it.id }.toList(),
          memberThumbnailMap,
          it.thumbNailUrl?.let { it1 -> fileService.generatePreSignedUrlToDownload(it1) }
        )
      },
      projects.pageable,
      projects.hasNext()
    )
  }

  @Transactional
  suspend fun addBookmark(userId: String, projectId: String) {
    val project = projectService.getProjectById(projectId)
    project.addBookmark(userId)
    projectService.update(project)
  }

  @Transactional
  suspend fun deleteBookmark(userId: String, projectId: String) {
    val project = projectService.getProjectById(projectId)
    project.deleteBookmark(userId)
    projectService.update(project)
  }

  @Transactional(readOnly = true)
  suspend fun getProjectByProjectId(
    requestedUserId: String,
    projectId: String
  ): ProjectWithUserResponseDto {
    val project = projectService.getProjectById(projectId)
    val user = userService.getUserById(project.userId)
    return ProjectWithUserResponseDto.from(
      project,
      user,
      requestedUserId,
      project.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      user.profileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    )
  }

  @Transactional
  suspend fun patchProjectTryoutStatusToAccept(
    userId: String,
    patchRequest: PatchProjectTryoutRequestDto
  ): ProjectTryoutResponseDto {
    checkUserHasAuthToProject(patchRequest.projectId, userId, ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN)
    val projectTryout = projectTryoutService.updateTryoutStatus(patchRequest.tryoutId, ProjectTryoutStatus.ACCEPTED)
    projectMemberService.save(projectTryout.createProjectMember())
    notificationService.createNotification(NotificationType.ACCEPTED, projectTryout.userId)

    return ProjectTryoutResponseDto.from(projectTryout)
  }

  @Transactional
  suspend fun pathProjectTryoutStatusToReject(
    userId: String,
    patchRequest: PatchProjectTryoutRequestDto
  ): ProjectTryoutResponseDto {
    checkUserHasAuthToProject(patchRequest.projectId, userId, ErrorCode.PROJECT_TRYOUT_VIEW_FORBIDDEN)
    val projectTryout = projectTryoutService.updateTryoutStatus(patchRequest.tryoutId, ProjectTryoutStatus.REJECTED)
    notificationService.createNotification(NotificationType.REJECTED, projectTryout.userId)

    return ProjectTryoutResponseDto.from(projectTryout)
  }

  @Transactional
  suspend fun deleteProjectMember(userId: String, projectId: String, memberId: String) {
    checkUserHasAuthToProject(projectId, userId, ErrorCode.PROJECT_MEMBER_MODIFY_FORBIDDEN)
    val projectMember = projectMemberService.findMemberById(memberId);
    projectMember.tryoutId?.let { tryoutId ->
      projectTryoutService.deleteTryout(tryoutId)
    }
    projectMemberService.deleteProjectMember(memberId)
  }

  @Transactional(readOnly = true)
  suspend fun getMyProjectSlice(userId: String, pageable: Pageable): Slice<GetMyProjectResponseDto> {
    val projects = projectService.getProjectSliceByUserIdOrderByCreatedAtDesc(userId, pageable)
    return SliceImpl(
      projects.content.map {
        GetMyProjectResponseDto.of(
          it,
          it.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
        )
      },
      projects.pageable,
      projects.hasNext()
    )
  }

  @Transactional(readOnly = true)
  suspend fun getCompleteProject(userId: String, projectId: String): CompletedProjectResponseDto {
    val project = checkUserHasAuthToProject(projectId, userId, ErrorCode.PROJECT_COMPLETE_FORBIDDEN)
    val thumbNailUrl =
      project.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    return CompletedProjectResponseDto.from(project, thumbNailUrl)
  }

  @Transactional
  suspend fun updateCompleteProject(
    userId: String,
    projectId: String,
    requestDto: CompleteProjectRequestDto
  ): CompletedProjectResponseDto {
    var project = checkUserHasAuthToProject(projectId, userId, ErrorCode.PROJECT_COMPLETE_FORBIDDEN)
    project = projectService.completeProject(project, requestDto)

    val projectMemberList = resumeService.getResumeListByResumeId(requestDto.projectMemberResumeId)
    projectMemberService.updateAllProjectMember(projectId, projectMemberList.map { it ->
      ProjectMember(
        resumeId = it.id!!,
        userId = it.userId,
        tryoutId = null,
        userName = it.userName,
        userSelfDescription = it.selfDescription.orEmpty(),
        userThumbnail = it.userProfileImageUrl,
        projectId = projectId,
        createdAt = LocalDateTime.now()
      )
    }.toList())

    val thumbNailUrl =
      project.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
    return CompletedProjectResponseDto.from(project, thumbNailUrl)
  }

  @Transactional(readOnly = true)
  suspend fun getMyProjectSliceInProgress(
    userId: String,
    pageable: Pageable
  ): Slice<GetMyInProgressProjectResponseDto> {
    val projects =
      projectService.getProjectsByProjectMemberUserIdAndIsCompletedOrderByCreatedAtDesc(userId, false, pageable)

    val projectIds = projects.content.mapNotNull { it.id }.toSet()

    val projectMembers = projectMemberService.findAllMembersByProjectId(projectIds)

    val membersByProjectId = projectMembers.groupBy { it.projectId }

    val responseDtos = projects.content.map { project ->
      val members = membersByProjectId[project.id!!] ?: emptyList()

      val myMember = members.find { it.userId == userId }
      val positionName = myMember?.positionName

      val userInfos = members.filter { it.userId != userId }
        .map { member ->
          ProjectMemberInfoDto.of(
            member.userId,
            member.userName,
            member.userThumbnail?.let { fileService.generatePreSignedUrlToDownload(it) }
          )
        }

      val thumbNailDownloadUrl = project.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }

      GetMyInProgressProjectResponseDto.of(
        project,
        positionName,
        thumbNailDownloadUrl,
        userInfos
      )
    }

    return SliceImpl(responseDtos, pageable, projects.hasNext())
  }

  suspend fun getMyProjectSliceInReview(
    userId: String,
    pageable: Pageable
  ): Slice<GetMyInReviewProjectResponseDto> {
    val projectsWithProjectTryout =
      projectService.getProjectsByProjectTryoutInReviewUserIdAndIsCompletedOrderByCreatedAtDesc(userId, false, pageable)

    val responseDtos = projectsWithProjectTryout.content.map { projectWithTryout ->
      val thumbNailDownloadUrl = projectWithTryout.thumbNailUrl?.let { fileService.generatePreSignedUrlToDownload(it) }
      GetMyInReviewProjectResponseDto.of(
        projectWithTryout.id,
        projectWithTryout.title,
        projectWithTryout.description,
        thumbNailDownloadUrl,
        projectWithTryout.positionName
      )
    }

    return SliceImpl(responseDtos, pageable, projectsWithProjectTryout.hasNext())
  }

  /**
  }   * 요청을 보낸 사용자가 해당 프밋의 생성자인지 검증한다.
   * 생성자가 맞는 경우엔 Project 를 반환한다.
   */
  private suspend fun checkUserHasAuthToProject(projectId: String, userId: String, errorCode: ErrorCode): Project {
    val project = projectService.getProjectById(projectId)
    if (project.userId != userId) {
      throw ForbiddenRequestException(errorCode)
    }
    return project
  }

}

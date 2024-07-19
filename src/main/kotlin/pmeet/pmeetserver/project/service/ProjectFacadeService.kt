package pmeet.pmeetserver.project.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.comment.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto

@Service
class ProjectFacadeService(
  private val projectService: ProjectService,
  private val projectCommentService: ProjectCommentService,
) {

  @Transactional
  suspend fun createProject(userId: String, requestDto: CreateProjectRequestDto): ProjectResponseDto {
    val recruitments =
      requestDto
        .recruitments
        .map { Recruitment(it.jobName, it.numberOfRecruitment) }
        .toList()

    val project = Project(
      userId = userId,
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      recruitments = recruitments,
      description = requestDto.description
    )

    return ProjectResponseDto.from(projectService.save(project))
  }

  @Transactional
  suspend fun updateProject(userId: String, requestDto: UpdateProjectRequestDto): ProjectResponseDto {
    val originalProject = projectService.getProjectById(requestDto.id)

    if (originalProject.userId != userId) {
      throw ForbiddenRequestException(ErrorCode.PROJECT_UPDATE_FORBIDDEN)
    }

    originalProject.update(
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      recruitments = requestDto.recruitments.map { Recruitment(it.jobName, it.numberOfRecruitment) },
      description = requestDto.description
    )

    return ProjectResponseDto.from(projectService.update(originalProject))
  }

  @Transactional
  suspend fun createProjectComment(userId: String, requestDto: CreateProjectCommentRequestDto):
    ProjectCommentResponseDto {

    val project = projectService.getProjectById(requestDto.projectId)

    val projectComment = ProjectComment(
      parentCommentId = requestDto.parentCommentId,
      projectId = project.id!!,
      userId = userId,
      content = requestDto.content
    )

    return ProjectCommentResponseDto.from(projectCommentService.save(projectComment))
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
}

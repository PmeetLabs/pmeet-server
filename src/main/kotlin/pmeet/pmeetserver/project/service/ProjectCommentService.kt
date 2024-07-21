package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentWithChildResponseDto
import pmeet.pmeetserver.project.repository.ProjectCommentRepository

@Service
class ProjectCommentService(
  private val projectCommentRepository: ProjectCommentRepository
) {

  @Transactional
  suspend fun save(projectComment: ProjectComment): ProjectComment {
    return projectCommentRepository.save(projectComment).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun getProjectCommentById(projectCommentId: String): ProjectComment {
    return projectCommentRepository.findById(projectCommentId).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.PROJECT_COMMENT_NOT_FOUND)
  }

  @Transactional
  suspend fun deleteAllByProjectId(projectId: String) {
    projectCommentRepository.deleteByProjectId(projectId).awaitSingleOrNull()
  }

  @Transactional(readOnly = true)
  suspend fun getProjectCommentWithChildByProjectId(projectId: String): List<ProjectCommentWithChildResponseDto> {
    val parentComments =
      projectCommentRepository.findByProjectIdAndParentCommentIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc(projectId)
        .collectList().awaitSingle()

    return parentComments.map { parentComment ->
      ProjectCommentWithChildResponseDto.from(
        parentComment,
        projectCommentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtDesc(parentComment.id!!)
          .collectList().awaitSingle()
      )
    }
  }
}

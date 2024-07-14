package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.project.domain.ProjectComment
import pmeet.pmeetserver.project.repository.ProjectCommentRepository

@Service
class ProjectCommentService(
  private val projectCommentRepository: ProjectCommentRepository
) {

  @Transactional
  suspend fun save(projectComment: ProjectComment): ProjectComment {
    return projectCommentRepository.save(projectComment).awaitSingle()
  }
}

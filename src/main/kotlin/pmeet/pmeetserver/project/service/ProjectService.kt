package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.repository.ProjectRepository

@Service
class ProjectService(
  private val projectRepository: ProjectRepository
) {

  @Transactional
  suspend fun save(project: Project): Project {
    return projectRepository.save(project).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun getProjectById(projectId: String): Project {
    return projectRepository.findById(projectId).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND)
  }

  @Transactional
  suspend fun update(project: Project): Project {
    return projectRepository.save(project).awaitSingle()
  }

  @Transactional
  suspend fun delete(project: Project) {
    projectRepository.delete(project).awaitSingleOrNull()
  }
}

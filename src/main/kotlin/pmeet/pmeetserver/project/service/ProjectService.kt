package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
}

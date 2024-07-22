package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.repository.ProjectTryoutRepository

@Service
class ProjectTryoutService(
  private val projectTryoutRepository: ProjectTryoutRepository
) {

  @Transactional
  suspend fun save(projectTryout: ProjectTryout): ProjectTryout {
    return projectTryoutRepository.save(projectTryout).awaitSingle()
  }

  @Transactional
  suspend fun deleteAllByProjectId(projectId: String) {
    projectTryoutRepository.deleteByProjectId(projectId).awaitSingleOrNull()
  }

}

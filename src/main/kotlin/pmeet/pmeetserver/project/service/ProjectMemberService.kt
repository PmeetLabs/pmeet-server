package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.project.domain.ProjectMember
import pmeet.pmeetserver.project.repository.ProjectMemberRepository

@Service
class ProjectMemberService(
  private val projectMemberRepository: ProjectMemberRepository
) {

  @Transactional
  suspend fun save(projectMember: ProjectMember): ProjectMember {
    return projectMemberRepository.save(projectMember).awaitSingle()
  }

  @Transactional
  suspend fun deleteProjectMember(projectMemberId: String) {
    projectMemberRepository.deleteById(projectMemberId).awaitSingleOrNull()
  }

  @Transactional
  suspend fun findMemberById(memberId: String): ProjectMember {
    return projectMemberRepository.findById(memberId).awaitSingle()
  }

  @Transactional
  suspend fun updateAllProjectMember(projectId: String, projectMemberList: List<ProjectMember>) {
    projectMemberRepository.deleteAllByProjectId(projectId).awaitSingle()
    projectMemberRepository.saveAll(projectMemberList).collectList().awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun findAllMembersByProjectId(projectIdSet: Set<String>): List<ProjectMember> {
    return projectMemberRepository.findAllByProjectIdIn(projectIdSet).collectList().awaitSingle()
  }
}

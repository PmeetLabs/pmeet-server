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

}
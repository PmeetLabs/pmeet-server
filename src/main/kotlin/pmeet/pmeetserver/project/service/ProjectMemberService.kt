package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
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

}
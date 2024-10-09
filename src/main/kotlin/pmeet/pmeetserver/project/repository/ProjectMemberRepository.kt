package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectMember
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProjectMemberRepository : ReactiveMongoRepository<ProjectMember, String> {

  fun findAllByProjectIdIn(projectId: Set<String>): Flux<ProjectMember>

  fun deleteAllByProjectId(projectId: String): Mono<Boolean>

}

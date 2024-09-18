package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectTryout
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProjectTryoutRepository : ReactiveMongoRepository<ProjectTryout, String> {
  fun deleteByProjectId(projectId: String): Mono<Void>

  fun findAllByProjectId(projectId: String): Flux<ProjectTryout>

  fun findAllByProjectIdAndTryoutStatusIsOrderByUpdatedAtDesc(
    projectId: String,
    tryoutStatus: ProjectTryoutStatus
  ): Flux<ProjectTryout>

}

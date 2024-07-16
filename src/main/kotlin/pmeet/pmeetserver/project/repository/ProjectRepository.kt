package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.Project
import reactor.core.publisher.Flux

interface ProjectRepository : ReactiveMongoRepository<Project, String> {
  fun findByUserId(userId: String): Flux<Project>
}

package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectComment
import reactor.core.publisher.Flux

interface ProjectCommentRepository : ReactiveMongoRepository<ProjectComment, String> {
  fun findByProjectId(projectId: String): Flux<ProjectComment>
}


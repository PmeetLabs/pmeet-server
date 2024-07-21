package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectComment
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface ProjectCommentRepository : ReactiveMongoRepository<ProjectComment, String> {

  fun deleteByProjectId(projectId: String): Mono<Void> // 프로젝트 ID로 삭제
  
  fun findByProjectId(projectId: String): Flux<ProjectComment>

}
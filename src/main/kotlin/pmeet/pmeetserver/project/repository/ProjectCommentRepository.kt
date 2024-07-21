package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectComment
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ProjectCommentRepository : ReactiveMongoRepository<ProjectComment, String> {
  fun findByProjectId(projectId: String): Flux<ProjectComment>
  fun findByProjectIdAndParentCommentIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc(projectId: String): Flux<ProjectComment>
  fun findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtDesc(parentId: String): Flux<ProjectComment>
  fun deleteByProjectId(projectId: String): Mono<Void> // 프로젝트 ID로 삭제
}

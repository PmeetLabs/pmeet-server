package pmeet.pmeetserver.project.repository

import pmeet.pmeetserver.project.dto.comment.ProjectCommentWithChildDto
import reactor.core.publisher.Flux

interface ProjectCommentRepositoryCustom {
  fun findCommentsByProjectIdWithChild(projectId: String): Flux<ProjectCommentWithChildDto>
}

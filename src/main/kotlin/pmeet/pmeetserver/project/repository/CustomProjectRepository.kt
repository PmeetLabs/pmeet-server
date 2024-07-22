package pmeet.pmeetserver.project.repository

import org.springframework.data.domain.Pageable
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.enums.ProjectFilterType
import reactor.core.publisher.Flux

interface CustomProjectRepository {

  fun findAllByFilter(
    isCompleted: Boolean,
    filterType: ProjectFilterType?,
    filterValue: String?,
    pageable: Pageable
  ): Flux<Project>
}

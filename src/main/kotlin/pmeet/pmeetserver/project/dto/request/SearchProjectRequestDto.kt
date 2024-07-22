package pmeet.pmeetserver.project.dto.request

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.enums.ProjectSortProperty

data class SearchProjectRequestDto(
  val isCompleted: Boolean,
  val filterType: ProjectFilterType? = null,
  val filterValue: String? = null,
  val pageable: Pageable
) {
  companion object {
    fun of(
      isCompleted: Boolean,
      filterType: ProjectFilterType?,
      filterValue: String?,
      page: Int,
      size: Int,
      sortBy: ProjectSortProperty,
      direction: Direction
    ): SearchProjectRequestDto {
      return SearchProjectRequestDto(
        isCompleted = isCompleted,
        filterType = filterType,
        filterValue = filterValue,
        pageable = PageRequest.of(page, size, Sort.by(direction, sortBy.property))
      )
    }
  }
}

package pmeet.pmeetserver.project.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Criteria
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.enums.ProjectFilterType
import reactor.core.publisher.Flux

class CustomProjectRepositoryImpl(
  @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomProjectRepository {

  companion object {
    private const val DOCUMENT_NAME = "project"
    private const val PROPERTY_NAME_BOOK_MARKERS = "bookmarkers"
    private const val PROPERTY_NAME_JOB_NAME = "recruitments.jobName"
    private const val PROPERTY_NAME_TITLE = "title"
    private const val PROPERTY_NAME_IS_COMPLETED = "isCompleted"
    private const val PROPERTY_NAME_BOOK_MARKERS_SIZE = "bookmarkersSize"
  }

  override fun findAllByFilter(
    isCompleted: Boolean,
    filterType: ProjectFilterType?,
    filterValue: String?,
    pageable: Pageable
  ): Flux<Project> {
    val criteria = createCriteria(filterType, filterValue)
    return aggregateProjects(isCompleted, criteria, pageable)
  }

  /**
   * 프로젝트 필터 조건인 Criteria 생성
   *
   * @param filterType ? 필터 타입(ALL, TITLE, JOB_NAME)
   * @param filterValue ? 필터 값
   */
  private fun createCriteria(filterType: ProjectFilterType?, filterValue: String?): Criteria {
    return if (filterType == null || filterValue == null) { // 필터가 없거나 값이 없는 경우 전체 조회
      Criteria()
    } else {
      when (filterType) {
        ProjectFilterType.ALL -> Criteria().orOperator(
          Criteria.where(PROPERTY_NAME_TITLE).regex(".*${filterValue}.*"),
          Criteria.where(PROPERTY_NAME_JOB_NAME).regex(".*${filterValue}.*")
        )

        ProjectFilterType.TITLE -> Criteria.where(PROPERTY_NAME_TITLE).regex(".*${filterValue}.*")
        ProjectFilterType.JOB_NAME -> Criteria.where(PROPERTY_NAME_JOB_NAME).regex(".*${filterValue}.*")
      }
    }
  }

  /**
   * 프로젝트 목록을 조회하기 위한 Aggregation을 수행
   *
   * @param isCompleted 완료 여부
   * @param criteria 검색 조건
   * @param pageable 페이징 정보
   */
  private fun aggregateProjects(isCompleted: Boolean, criteria: Criteria, pageable: Pageable): Flux<Project> {
    val aggregation = generateSearchAggregation(isCompleted, criteria, pageable)
    return mongoTemplate.aggregate(aggregation, DOCUMENT_NAME, Project::class.java)
  }

  /**
   * 프로젝트 검색을 위한 Aggregation 생성
   *
   * @param isCompleted 완료 여부
   * @param criteria 검색 조건
   * @param pageable 페이징 정보
   */
  private fun generateSearchAggregation(isCompleted: Boolean, criteria: Criteria, pageable: Pageable): Aggregation {
    val newCriteria = Criteria.where(PROPERTY_NAME_IS_COMPLETED).`is`(isCompleted).andOperator(criteria)

    val match = Aggregation.match(newCriteria)

    val addFields = Aggregation.addFields()
      .addField(PROPERTY_NAME_BOOK_MARKERS_SIZE)
      .withValue(ArrayOperators.Size.lengthOfArray(PROPERTY_NAME_BOOK_MARKERS))
      .build()

    val sort = if (pageable.sort.getOrderFor(PROPERTY_NAME_BOOK_MARKERS) != null) {
      val direction = pageable.sort.getOrderFor(PROPERTY_NAME_BOOK_MARKERS)!!.direction
      Aggregation.sort(Sort.by(direction, PROPERTY_NAME_BOOK_MARKERS_SIZE))
    } else {
      Aggregation.sort(pageable.sort)
    }

    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    return Aggregation.newAggregation(match, addFields, sort, skip, limit)
  }
}

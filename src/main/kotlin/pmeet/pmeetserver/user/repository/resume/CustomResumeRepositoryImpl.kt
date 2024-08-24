package pmeet.pmeetserver.user.repository.resume

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
import org.springframework.data.mongodb.core.aggregation.DateOperators
import org.springframework.data.mongodb.core.query.Criteria
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.resume.ResumeBookMarker
import reactor.core.publisher.Flux

class CustomResumeRepositoryImpl(
  @Autowired private var mongoTemplate: ReactiveMongoTemplate
) : CustomResumeRepository {

  companion object {
    private const val DOCUMENT_NAME = "resume"
    private const val PROPERTY_NAME_BOOK_MARKERS = "bookmarkers"
    private const val PROPERTY_NAME_BOOK_MARKERS_SIZE = "bookmarkersSize"
    private const val PROPERTY_NAME_UPDATEDAT = "updatedAt"
    private const val PROPERTY_NAME_TITLE = "title"
    private const val PROPERTY_NAME_NICKNAME = "userName"
    private const val PROPERTY_NAME_JOB_NAME = "desiredJobs.name"
    private const val PROPERTY_NAME_ISACTIVE = "isActive"
  }

  override fun findAllByFilter(
    filterType: ResumeFilterType,
    filterValue: String,
    orderType: ResumeOrderType,
    pageable: Pageable
  ): Flux<Resume> {
    val criteria = createCriteria(filterType, filterValue)
    return aggregateResumes(orderType, criteria, pageable)
  }

  /**
   * 이력서 필터 조건인 Criteria 생성
   *
   * @param filterType 필터 타입(TOTAL, TITLE, JOB, NICKNAME)
   * @param filterValue 필터 값
   */
  private fun createCriteria(filterType: ResumeFilterType, filterValue: String): Criteria {
    return if (filterValue == "") {
      Criteria()
    } else {
      when (filterType) {
        ResumeFilterType.ALL -> Criteria().orOperator(
          Criteria.where(PROPERTY_NAME_TITLE).regex(".*${filterValue}.*"),
          Criteria.where(PROPERTY_NAME_JOB_NAME).regex(".*${filterValue}.*"),
          Criteria.where(PROPERTY_NAME_NICKNAME).regex(".*${filterValue}.*"),
        )

        ResumeFilterType.TITLE -> Criteria.where(PROPERTY_NAME_TITLE).regex(".*${filterValue}.*")
        ResumeFilterType.JOB -> Criteria.where(PROPERTY_NAME_JOB_NAME).regex(".*${filterValue}.*")
        ResumeFilterType.NICKNAME -> Criteria.where(PROPERTY_NAME_NICKNAME).regex(".*${filterValue}.*")
      }
    }
  }

  /**
   * 이력서 목록을 조회하기 위한 aggregation 수행
   * @param orderType 정렬 조건
   * @param criteria 검색 조건
   * @param pageable 페이징 정보
   */
  private fun aggregateResumes(orderType: ResumeOrderType, criteria: Criteria, pageable: Pageable): Flux<Resume> {
    val aggregation = generateSearchAggregation(orderType, criteria, pageable)
    return mongoTemplate.aggregate<Resume>(aggregation, DOCUMENT_NAME, Resume::class.java)
  }

  /**
   * 이력서 검색을 위한 Aggregation 생성
   *
   * @param orderType 정렬 조건
   * @param criteria 검색 조건
   * @param pageable 페이징 정보
   */
  private fun generateSearchAggregation(
    orderType: ResumeOrderType,
    criteria: Criteria,
    pageable: Pageable
  ): Aggregation {
    val updatedCriteria = Criteria.where(PROPERTY_NAME_ISACTIVE).`is`(true).andOperator(criteria)
    val match = Aggregation.match(updatedCriteria)

    val addFields = Aggregation.addFields()
      .addField(PROPERTY_NAME_BOOK_MARKERS_SIZE)
      .withValue(
        ArrayOperators.Size.lengthOfArray(
          ConditionalOperators.ifNull(PROPERTY_NAME_BOOK_MARKERS).then(emptyList<ResumeBookMarker>())
        )
      )
      .build()

    val sort = when (orderType) {
      ResumeOrderType.POPULAR -> Aggregation.sort(Sort.by(Sort.Direction.DESC, PROPERTY_NAME_BOOK_MARKERS_SIZE))
      ResumeOrderType.RECENT -> Aggregation.sort(Sort.by(Sort.Direction.DESC, PROPERTY_NAME_UPDATEDAT))
    }

    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    return Aggregation.newAggregation(match, addFields, sort, skip, limit)
  }

}

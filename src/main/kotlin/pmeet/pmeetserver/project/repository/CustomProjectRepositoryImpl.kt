package pmeet.pmeetserver.project.repository

import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
import org.springframework.data.mongodb.core.query.Criteria
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectBookmark
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.repository.vo.ProjectWithProjectTryout
import reactor.core.publisher.Flux


class CustomProjectRepositoryImpl(
  @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomProjectRepository {

  companion object {
    private const val DOCUMENT_NAME_PROJECT = "project"
    private const val DOCUMENT_NAME_PROJECT_MEMBER = "projectMember"
    private const val DOCUMENT_NAME_PROJECT_TRYOUT = "projectTryout"

    private const val PROPERTY_NAME_ID = "_id"
    private const val PROPERTY_NAME_BOOK_MARKERS = "bookmarkers"
    private const val PROPERTY_NAME_CREATOR_ID = "userId"
    private const val PROPERTY_NAME_JOB_NAME = "recruitments.jobName"
    private const val PROPERTY_NAME_TITLE = "title"
    private const val PROPERTY_NAME_IS_COMPLETED = "isCompleted"
    private const val PROPERTY_NAME_BOOK_MARKERS_SIZE = "bookmarkersSize"
    private const val PROPERTY_NAME_USER_ID = "userId"
    private const val PROPERTY_NAME_CREATED_AT = "createdAt"
    private const val PROPERTY_NAME_PROJECT_ID = "projectId"
    private const val PROPERTY_NAME_PROJECT_TRYOUT_STATUS = "tryoutStatus"
  }

  override fun findAllByFilter(
    isCompleted: Boolean,
    filterType: ProjectFilterType?,
    filterValue: String?,
    userId: String,
    isMy: Boolean?,
    pageable: Pageable
  ): Flux<Project> {
    val criteria = createCriteria(filterType, filterValue, userId, isMy)
    return aggregateProjects(isCompleted, criteria, pageable)
  }

  override fun findProjectByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Flux<Project> {
    val criteria = Criteria.where(PROPERTY_NAME_USER_ID).`is`(userId)
    val sort = Sort.by(Sort.Order.desc(PROPERTY_NAME_CREATED_AT))

    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    return mongoTemplate.aggregate(
      Aggregation.newAggregation(
        Aggregation.match(criteria),
        Aggregation.sort(sort),
        skip,
        limit
      ),
      DOCUMENT_NAME_PROJECT,
      Project::class.java
    )
  }

  override fun findProjectsByProjectMemberUserIdAndIsCompletedOrderByCreatedAtDesc(
    userId: String,
    isCompleted: Boolean,
    pageable: Pageable
  ): Flux<Project> {
    val criteria = Criteria.where(PROPERTY_NAME_USER_ID).`is`(userId)

    val addFields = AggregationOperation { context ->
      Document.parse("{ \$addFields: { projectId: { \$toObjectId: \"\$projectId\" } } }")
    }

    val lookup = Aggregation.lookup(
      DOCUMENT_NAME_PROJECT,
      PROPERTY_NAME_PROJECT_ID,
      PROPERTY_NAME_ID,
      DOCUMENT_NAME_PROJECT
    )

    val unwind = Aggregation.unwind(DOCUMENT_NAME_PROJECT)
    val replaceRoot = Aggregation.replaceRoot("project")

    val sort = Sort.by(Sort.Order.desc(PROPERTY_NAME_CREATED_AT))
    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    val newAggregation = Aggregation.newAggregation(
      Aggregation.match(criteria),
      Aggregation.sort(sort),
      skip,
      limit,
      addFields,
      lookup,
      unwind,
      replaceRoot,
      Aggregation.match(Criteria.where(PROPERTY_NAME_IS_COMPLETED).`is`(isCompleted))
    )

    val aggregate = mongoTemplate.aggregate(
      newAggregation,
      DOCUMENT_NAME_PROJECT_MEMBER,
      Project::class.java
    )

    return aggregate
  }

  override fun findProjectsByProjectTryoutUserIdAndIsCompletedOrderByCreatedAtDesc(
    userId: String,
    isCompleted: Boolean,
    tryoutStatus: ProjectTryoutStatus,
    pageable: Pageable
  ): Flux<ProjectWithProjectTryout> {
    val criteria = Criteria.where(PROPERTY_NAME_USER_ID).`is`(userId).and(PROPERTY_NAME_PROJECT_TRYOUT_STATUS)
      .`is`(tryoutStatus)

    val addFields = AggregationOperation { context ->
      Document.parse("{ \$addFields: { projectId: { \$toObjectId: \"\$projectId\" } } }")
    }

    val lookup = Aggregation.lookup(
      DOCUMENT_NAME_PROJECT,
      PROPERTY_NAME_PROJECT_ID,
      PROPERTY_NAME_ID,
      DOCUMENT_NAME_PROJECT
    )

    val unwind = Aggregation.unwind(DOCUMENT_NAME_PROJECT)

    val sort = Sort.by(Sort.Order.desc(PROPERTY_NAME_CREATED_AT))
    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    val projectStage = Aggregation.project()
      .and("project._id").`as`("id")
      .and("project.userId").`as`("projectCreatedBy")
      .and("project.title").`as`("title")
      .and("project.thumbNailUrl").`as`("thumbNailUrl")
      .and("project.description").`as`("description")
      .and("project.isCompleted").`as`("isCompleted")
      .and("resumeId").`as`("resumeId")
      .and("userId").`as`("userId")
      .and("userName").`as`("userName")
      .and("userSelfDescription").`as`("userSelfDescription")
      .and("userProfileImageUrl").`as`("userProfileImageUrl")
      .and("positionName").`as`("positionName")
      .and("tryoutStatus").`as`("tryoutStatus")

    val newAggregation = Aggregation.newAggregation(
      Aggregation.match(criteria),
      Aggregation.sort(sort),
      skip,
      limit,
      addFields,
      lookup,
      unwind,
      projectStage,
      Aggregation.match(Criteria.where(PROPERTY_NAME_IS_COMPLETED).`is`(isCompleted))
    )

    val aggregate = mongoTemplate.aggregate(
      newAggregation,
      DOCUMENT_NAME_PROJECT_TRYOUT,
      ProjectWithProjectTryout::class.java
    )

    return aggregate
  }

  /**
   * 프로젝트 필터 조건인 Criteria 생성
   *
   * @param filterType ? 필터 타입(ALL, TITLE, JOB_NAME)
   * @param filterValue ? 필터 값
   */
  private fun createCriteria(
    filterType: ProjectFilterType?,
    filterValue: String?,
    userId: String,
    isMy: Boolean?
  ): Criteria {
    val baseCriteria = if (filterType == null || filterValue == null) {
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

    return if (isMy == true) {
      baseCriteria.and(PROPERTY_NAME_CREATOR_ID).`is`(userId)
    } else {
      baseCriteria
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
    return mongoTemplate.aggregate(aggregation, DOCUMENT_NAME_PROJECT, Project::class.java)
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
      .withValue(
        ArrayOperators.Size.lengthOfArray(
          ConditionalOperators.IfNull.ifNull(PROPERTY_NAME_BOOK_MARKERS).then(emptyList<ProjectBookmark>())
        )
      )
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

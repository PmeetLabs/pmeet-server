package pmeet.pmeetserver.project.repository

import java.time.LocalDateTime
import java.time.ZoneId
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.addFields
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext
import org.springframework.data.mongodb.core.query.Criteria
import pmeet.pmeetserver.project.dto.comment.response.ProjectCommentResponseDto
import pmeet.pmeetserver.project.dto.comment.ProjectCommentWithChildDto
import reactor.core.publisher.Flux

class ProjectCommentRepositoryCustomImpl(
  @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : ProjectCommentRepositoryCustom {
  override fun findCommentsByProjectIdWithChild(projectId: String): Flux<ProjectCommentWithChildDto> {

    val matchProjectId = match(Criteria.where("projectId").`is`(projectId))

    val lookupChildComments = AggregationOperation { context: AggregationOperationContext ->
      Document(
        "\$lookup", Document("from", "projectComment")
          .append(
            "let",
            Document("id", Document("\$toString", "\$_id")) // _id를 String으로 변환 후 id로 저장
          )
          .append(
            "pipeline", listOf(
              Document(
                "\$match",
                Document("\$expr", Document("\$eq", listOf("\$parentCommentId", "\$\$id")))
              )
            )
          )
          .append("as", "childComments")
      )
    }

    val filterExpression = Document(
      "\$filter",
      Document("input", "\$childComments")
        .append("as", "child")
        .append("cond", Document("\$eq", listOf("\$\$child.isDeleted", false)))
    )

    val addNonDeletedChildComments = addFields()
      .addFieldWithValue("childComments", filterExpression).build()

    val matchNonDeletedOrWithNonDeletedChildren = match(
      Criteria().andOperator(
        Criteria.where("parentCommentId").isNull,
        Criteria().orOperator(
          Criteria.where("isDeleted").`is`(false),
          Criteria().andOperator(
            Criteria.where("isDeleted").`is`(true),
            Criteria.where("childComments").ne(emptyList<Any>())
          )
        )
      )
    )

    val sortByCreatedAtDesc = sort(Sort.Direction.DESC, "createdAt")

    val aggregation = newAggregation(
      matchProjectId,
      lookupChildComments,
      addNonDeletedChildComments,
      matchNonDeletedOrWithNonDeletedChildren,
      sortByCreatedAtDesc
    )

    return mongoTemplate.aggregate(aggregation, "projectComment", Document::class.java)
      .map { doc ->
        ProjectCommentWithChildDto(
          id = doc.getObjectId("_id")!!.toString(),
          parentCommentId = doc.getString("parentCommentId"),
          projectId = doc.getString("projectId"),
          userId = doc.getString("userId"),
          content = doc.getString("content"),
          likerIdList = doc.getList("likerIdList", String::class.java),
          createdAt = doc.getDate("createdAt")!!.toInstant().atZone(ZoneId.systemDefault())!!.toLocalDateTime(),
          isDeleted = doc.getBoolean("isDeleted"),
          childComments = doc.getList("childComments", Document::class.java)?.let { childComments ->
            childComments.map { childDoc ->
              ProjectCommentResponseDto(
                id = childDoc.getObjectId("_id")?.toString() ?: "",
                parentCommentId = childDoc.getString("parentCommentId") ?: "",
                projectId = childDoc.getString("projectId") ?: "",
                userId = childDoc.getString("userId") ?: "",
                content = childDoc.getString("content") ?: "",
                likerIdList = childDoc.getList("likerIdList", String::class.java) ?: emptyList(),
                createdAt = childDoc.getDate("createdAt")?.toInstant()?.atZone(ZoneId.systemDefault())
                  ?.toLocalDateTime() ?: LocalDateTime.now(),
                isDeleted = childDoc.getBoolean("isDeleted") ?: false
              )
            }
          } ?: emptyList()
        )
      }
  }
}

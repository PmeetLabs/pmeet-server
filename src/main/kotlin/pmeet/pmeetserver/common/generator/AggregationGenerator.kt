package pmeet.pmeetserver.common.generator

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria

object AggregationGenerator {

  fun generateAggregationFindByNameSearchSlice(name: String?, pageable: Pageable): Aggregation {
    val criteria = if (name != null) {
      Criteria.where("name").regex(".*${name}.*", "i")
    } else {
      Criteria()
    }

    val match = Aggregation.match(criteria)

    val addFields = Aggregation.project("name")
      .andExpression("strLenCP(name)").`as`("nameLength")

    val sort = Aggregation.sort(
      Sort.by(
        Sort.Order(Sort.Direction.ASC, "nameLength")
      ).and(
        Sort.by(Sort.Direction.ASC, "name")
      )
    )

    val limit = Aggregation.limit(pageable.pageSize.toLong() + 1)
    val skip = Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong())

    return Aggregation.newAggregation(match, addFields, sort, skip, limit)
  }

}

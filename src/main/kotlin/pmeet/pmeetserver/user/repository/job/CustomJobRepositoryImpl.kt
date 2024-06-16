package pmeet.pmeetserver.user.repository.job

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import pmeet.pmeetserver.common.generator.AggregationGenerator
import pmeet.pmeetserver.user.domain.job.Job
import reactor.core.publisher.Flux

class CustomJobRepositoryImpl(
  @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomJobRepository {

  override fun findByNameSearchSlice(name: String?, pageable: Pageable): Flux<Job> {

    val aggregation = AggregationGenerator.generateAggregationFindByNameSearchSlice(name, pageable)

    return mongoTemplate.aggregate(aggregation, "job", Job::class.java)
  }
}

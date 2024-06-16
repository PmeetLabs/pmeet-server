package pmeet.pmeetserver.user.repository.techStack

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import pmeet.pmeetserver.common.generator.AggregationGenerator
import pmeet.pmeetserver.user.domain.techStack.TechStack
import reactor.core.publisher.Flux

class CustomTechStackRepositoryImpl(
  @Autowired private val mongoTemplate: ReactiveMongoTemplate
) : CustomTechStackRepository {

  override fun findByNameSearchSlice(name: String?, pageable: Pageable): Flux<TechStack> {

    val aggregation = AggregationGenerator.generateAggregationFindByNameSearchSlice(name, pageable)

    return mongoTemplate.aggregate(aggregation, "techStack", TechStack::class.java)
  }
}
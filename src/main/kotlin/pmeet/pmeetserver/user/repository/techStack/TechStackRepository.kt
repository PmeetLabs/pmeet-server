package pmeet.pmeetserver.user.repository.techStack

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.techStack.TechStack
import reactor.core.publisher.Mono

interface TechStackRepository : ReactiveMongoRepository<TechStack, String>, CustomTechStackRepository {

  fun findByName(name: String): Mono<TechStack>

}
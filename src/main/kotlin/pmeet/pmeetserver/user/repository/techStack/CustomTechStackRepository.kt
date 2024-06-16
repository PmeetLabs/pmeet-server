package pmeet.pmeetserver.user.repository.techStack

import org.springframework.data.domain.Pageable
import pmeet.pmeetserver.user.domain.techStack.TechStack
import reactor.core.publisher.Flux

interface CustomTechStackRepository {

  fun findByNameSearchSlice(name: String?, pageable: Pageable): Flux<TechStack>

}
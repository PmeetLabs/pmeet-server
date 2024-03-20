package pmeet.pmeetserver.user.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.User
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<User, String> {
  fun findByEmail(email: String): Mono<User>
  fun findByNickname(nickname: String): Mono<User>

}

package pmeet.pmeetserver.user.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.User

interface UserRepository : ReactiveMongoRepository<User, String> {

}

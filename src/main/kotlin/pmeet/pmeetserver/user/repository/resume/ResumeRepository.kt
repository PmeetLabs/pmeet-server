package pmeet.pmeetserver.user.repository.resume

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.resume.Resume
import reactor.core.publisher.Mono

interface ResumeRepository : ReactiveMongoRepository<Resume, String> {

  fun countByUserId(userId: String): Mono<Long>

  fun findByIdAndUserId(id: String, userId: String): Mono<Resume>

  fun deleteByIdAndUserId(id: String, userId: String): Mono<Void>
}
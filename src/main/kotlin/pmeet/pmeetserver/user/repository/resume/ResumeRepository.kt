package pmeet.pmeetserver.user.repository.resume

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.resume.Resume
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ResumeRepository : ReactiveMongoRepository<Resume, String> {

  fun countByUserId(userId: String): Mono<Long>

  fun findByIdAndUserId(id: String, userId: String): Mono<Resume>

  fun findAllByUserId(userId: String): Flux<Resume>

  fun findAllByIdIn(resumeIdList: List<String>): Flux<Resume>
}
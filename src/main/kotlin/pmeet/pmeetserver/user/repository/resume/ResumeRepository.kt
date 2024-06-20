package pmeet.pmeetserver.user.repository.resume

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.resume.Resume
import reactor.core.publisher.Mono

interface ResumeRepository : ReactiveMongoRepository<Resume, String> {

}
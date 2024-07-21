package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectTryout

interface ProjectTryoutRepository : ReactiveMongoRepository<ProjectTryout, String> {

}
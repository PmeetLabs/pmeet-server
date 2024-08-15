package pmeet.pmeetserver.project.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.project.domain.ProjectMember

interface ProjectMemberRepository : ReactiveMongoRepository<ProjectMember, String> {

}
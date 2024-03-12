package pmeet.pmeetserver.member.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.member.domain.Member

interface MemberRepository : ReactiveMongoRepository<Member, String> {

}

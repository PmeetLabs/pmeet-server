package pmeet.pmeetserver.member.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Member(
  @Id
  val id: String? = null, // mongodb auto id generation
  val email: String,
  val password: String,
  val nickname: String
) {
}

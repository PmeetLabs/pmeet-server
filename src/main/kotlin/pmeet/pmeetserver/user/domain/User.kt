package pmeet.pmeetserver.user.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class User(
  @Id
  val id: String? = null, // mongodb auto id generation
  val provider: String? = null,
  val email: String,
  val name: String,
  val password: String? = null,
  val nickname: String
)

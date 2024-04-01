package pmeet.pmeetserver.user.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class User(
  @Id
  val id: String? = null, // mongodb auto id generation
  val provider: String? = null,
  val email: String,
  var name: String,
  var password: String? = null,
  var nickname: String,
  var isEmployed: Boolean = false,
  var profileImageUrl: String? = null
) {

  fun changePassword(password: String) {
    this.password = password
  }
}

package pmeet.pmeetserver.user.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.user.domain.enum.Gender
import java.time.LocalDate

@Document
class User(
  @Id
  var id: String? = null, // mongodb auto id generation
  val provider: String? = null,
  val email: String,
  var name: String,
  var phoneNumber: String? = null,
  var birthDate: LocalDate? = null,
  var gender: Gender? = null,
  var introductionComment: String? = null,
  var password: String? = null,
  var nickname: String,
  var nicknameNumber: Int? = null,
  var isEmployed: Boolean = false,
  var profileImageUrl: String? = null,
  var isDeleted: Boolean = false
) {

  fun changePassword(password: String) {
    this.password = password
  }

  fun updateUser(
    profileImageUrl: String?,
    name: String,
    nickname: String,
    phoneNumber: String?,
    birthDate: LocalDate?,
    gender: Gender?,
    isEmployed: Boolean,
    introductionComment: String?,
  ) {
    this.profileImageUrl = profileImageUrl
    this.name = name
    this.nickname = nickname
    this.phoneNumber = phoneNumber
    this.birthDate = birthDate
    this.gender = gender
    this.gender = gender
    this.isEmployed = isEmployed
    this.introductionComment = introductionComment
  }

  fun delete() {
    this.isDeleted = true
  }

}

package pmeet.pmeetserver.user.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.user.domain.enum.Gender
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 좋아요를 누른 다른 사람의 이력서 id
 */
data class ResumeBookmark(
  val resumeId: String,
  val addedAt: LocalDateTime
)

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
  var bookmarkedResumes: MutableList<ResumeBookmark> = mutableListOf(),
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

  fun addBookmarkForResume(resumeId: String) {
    val newResume = ResumeBookmark(resumeId, LocalDateTime.now())
    bookmarkedResumes.indexOfFirst { it.resumeId == resumeId }
      .takeIf { it != -1 }
      ?.let { bookmarkedResumes[it] = newResume }
      ?: bookmarkedResumes.add(newResume)
  }

  fun deleteBookmarkForResume(resumeId: String) {
    bookmarkedResumes.removeIf { it.resumeId == resumeId }
  }
}

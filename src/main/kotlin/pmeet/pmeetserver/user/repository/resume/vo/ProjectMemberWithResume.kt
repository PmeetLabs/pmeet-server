package pmeet.pmeetserver.user.repository.resume.vo

import pmeet.pmeetserver.user.domain.resume.Resume
import java.time.LocalDateTime

data class ProjectMemberWithResume(
  val id: String?,
  val resumeId: String,
  val userId: String,
  val userName: String,
  val userThumbnail: String?,
  val userSelfDescription: String,
  val positionName: String?,
  val projectId: String,
  val createdAt: LocalDateTime,
  val resume: Resume
)

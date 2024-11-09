package pmeet.pmeetserver.project.repository.vo

import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus

data class ProjectWithProjectTryout(
  var id: String,
  val projectCreatedBy: String,
  var title: String,
  var thumbNailUrl: String? = null,
  var description: String,
  var isCompleted: Boolean = false,
  val resumeId: String,
  val userId: String,
  val userName: String,
  val userSelfDescription: String,
  val userProfileImageUrl: String? = null,
  val positionName: String,
  var tryoutStatus: ProjectTryoutStatus,
)

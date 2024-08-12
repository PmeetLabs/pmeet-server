package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.project.domain.enum.ProjectTryoutStatus
import java.time.LocalDateTime

@Document
class ProjectTryout(
  @Id
  var id: String? = null,
  val resumeId: String,
  val userId: String,
  val userName: String,
  val userSelfDescription: String,
  val positionName: String,
  val tryoutStatus: ProjectTryoutStatus,
  val projectId: String,
  val createdAt: LocalDateTime,
)
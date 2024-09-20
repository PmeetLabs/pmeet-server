package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class ProjectMember(
  @Id
  var id: String? = null,
  val resumeId: String,
  val tryoutId: String? = null,
  val userId: String,
  val userName: String,
  val userThumbnail: String? = null,
  val userSelfDescription: String,
  var positionName: String? = null,
  val projectId: String,
  val createdAt: LocalDateTime,
) {
}

package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class ProjectMember(
  @Id
  var id: String? = null,
  val resumeId: String? = null,
  val tryoutId: String? = null,
  val userId: String,
  val userName: String,
  val userThumbnail: String? = null,
  val userSelfDescription: String? = null,
  var positionName: String? = null,
  val projectId: String,
  val createdAt: LocalDateTime,
) {
}

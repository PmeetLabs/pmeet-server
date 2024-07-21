package pmeet.pmeetserver.project.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class ProjectTryout(
  @Id
  var id: String? = null,
  val resumeId: String,
  val userId: String,
  val projectId: String,
  val createdAt: LocalDateTime,
)
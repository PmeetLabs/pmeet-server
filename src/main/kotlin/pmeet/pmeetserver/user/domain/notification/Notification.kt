package pmeet.pmeetserver.user.domain.notification

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pmeet.pmeetserver.user.domain.enum.NotificationType
import java.time.LocalDateTime

@Document
class Notification(
  @Id
  var id: String? = null,
  val notificationType: NotificationType,
  val targetUserId: String,
  val isRead: Boolean = false,
  val createdAt: LocalDateTime = LocalDateTime.now(),
) {

}

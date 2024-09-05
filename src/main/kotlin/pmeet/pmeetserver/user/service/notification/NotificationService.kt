package pmeet.pmeetserver.user.service.notification

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.repository.notification.NotificationRepository

@Service
class NotificationService(
  private val notificationRepository: NotificationRepository
) {

  @Transactional
  suspend fun createNotification(notificationType: NotificationType, userId: String): Notification {
    val notification = Notification(notificationType = notificationType, targetUserId = userId)
    return notificationRepository.save(notification).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun findUnreadNotificationByUserId(userId: String): List<Notification> {
    return notificationRepository.findAllByTargetUserIdAndIsReadFalse(userId).collectList().awaitSingle()
  }

}

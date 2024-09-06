package pmeet.pmeetserver.user.service.notification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.codec.ServerSentEvent
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
    val savedNotification = notificationRepository.save(notification).awaitSingle()
    notificationRepository.emitNotification(savedNotification)
    return savedNotification
  }

  fun subscribeToUserNotifications(userId: String): Flow<ServerSentEvent<Notification>> {
    val existingNotifications = notificationRepository.findAllByTargetUserIdAndIsReadFalse(userId)
      .asFlow()
      .map { notification -> ServerSentEvent.builder(notification).build() }

    val newNotifications = notificationRepository.subscribeToUserNotifications(userId)
      .asFlow()

    return merge(existingNotifications, newNotifications)
  }

  suspend fun markNotificationAsRead(id: String): Notification {
    val notification = notificationRepository.findById(id).awaitSingle()
    notification.isRead = true
    return notificationRepository.save(notification).awaitSingle()
  }

  suspend fun getUnreadNotificationsCount(userId: String): Int {
    return notificationRepository.findAllByTargetUserIdAndIsReadFalse(userId).collectList().awaitSingle().size
  }
}


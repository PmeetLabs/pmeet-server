package pmeet.pmeetserver.user.repository.notification

import org.springframework.http.codec.ServerSentEvent
import pmeet.pmeetserver.user.domain.notification.Notification
import reactor.core.publisher.Flux

interface CustomNotificationRepository {
  fun subscribeToUserNotifications(userId: String): Flux<ServerSentEvent<Notification>>
  fun emitNotification(notification: Notification)
}

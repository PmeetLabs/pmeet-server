package pmeet.pmeetserver.user.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.service.notification.NotificationService

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
  private val notificationService: NotificationService
) {

  @GetMapping("/subscribe/{userId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
  fun subscribeToNotifications(@PathVariable userId: String): Flow<ServerSentEvent<Notification>> {
    return notificationService.subscribeToUserNotifications(userId)
  }

  @PutMapping("/{id}/read")
  suspend fun markNotificationAsRead(@PathVariable id: String): Notification {
    return notificationService.markNotificationAsRead(id)
  }

  @GetMapping("/unread-count/{userId}")
  suspend fun getUnreadNotificationsCount(@PathVariable userId: String): Int {
    return notificationService.getUnreadNotificationsCount(userId)
  }
}

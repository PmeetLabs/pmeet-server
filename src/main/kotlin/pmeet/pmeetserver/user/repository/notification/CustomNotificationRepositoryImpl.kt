package pmeet.pmeetserver.user.repository.notification

import org.springframework.http.codec.ServerSentEvent
import pmeet.pmeetserver.user.domain.notification.Notification
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks


class CustomNotificationRepositoryImpl : CustomNotificationRepository {
  private val sinks = mutableMapOf<String, Sinks.Many<ServerSentEvent<Notification>>>()

  override fun subscribeToUserNotifications(userId: String): Flux<ServerSentEvent<Notification>> {
    val sink = sinks.getOrPut(userId) { Sinks.many().multicast().onBackpressureBuffer() }
    return sink.asFlux().doOnCancel {
      sinks.remove(userId)
    }
  }

  override fun emitNotification(notification: Notification) {
    sinks[notification.targetUserId]?.tryEmitNext(
      ServerSentEvent.builder(notification).build()
    )
  }
}

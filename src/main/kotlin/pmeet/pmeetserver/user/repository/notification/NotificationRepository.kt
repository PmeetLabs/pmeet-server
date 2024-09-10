package pmeet.pmeetserver.user.repository.notification

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.notification.Notification
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NotificationRepository : ReactiveMongoRepository<Notification, String>, CustomNotificationRepository {

  fun findAllByTargetUserIdAndIsReadFalse(userId: String): Flux<Notification>

  fun findAllByTargetUserId(userId: String): Flux<Notification>

  fun deleteAllByTargetUserId(userId: String): Mono<Boolean>
}

package pmeet.pmeetserver.user.repository.notification

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import pmeet.pmeetserver.user.domain.notification.Notification
import reactor.core.publisher.Flux

interface NotificationRepository : ReactiveMongoRepository<Notification, String> {

  fun findAllByTargetUserIdAndIsReadFalse(userId: String): Flux<Notification>

}

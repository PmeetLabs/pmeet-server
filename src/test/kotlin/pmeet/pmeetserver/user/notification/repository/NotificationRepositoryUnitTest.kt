package pmeet.pmeetserver.user.notification.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import pmeet.pmeetserver.config.BaseMongoDBTestForRepository
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.repository.notification.NotificationRepository

@ExperimentalCoroutinesApi
class NotificationRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : BaseMongoDBTestForRepository({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)

  val notificationRepository = factory.getRepository(NotificationRepository::class.java)

  var commentNotification: Notification

  val testUserId = "test-user-id"

  beforeSpec {
    commentNotification = Notification(notificationType = NotificationType.COMMENT, targetUserId = testUserId)

    notificationRepository.save(commentNotification).block()
  }

  afterSpec {
    Dispatchers.resetMain()
    notificationRepository.deleteAll().block()
  }

  describe("findAllByTargetUserIdAndIsReadFalse") {
    context("targetUserId 가 주어지면") {
      it("해당하는 알림을 반환한다") {
        runTest {

          val result = notificationRepository.findAllByTargetUserIdAndIsReadFalse(testUserId).collectList().block()

          result?.size shouldBe 1
          result?.get(0)?.notificationType shouldBe NotificationType.COMMENT
          result?.get(0)?.targetUserId shouldBe testUserId
        }
      }
    }
  }

})

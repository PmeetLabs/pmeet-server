package pmeet.pmeetserver.user.notification.service

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.repository.notification.NotificationRepository
import pmeet.pmeetserver.user.service.notification.NotificationService
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
class NotificationServiceUnitTest : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val notificationRepository = mockk<NotificationRepository>(relaxed = true)

  lateinit var notificationService: NotificationService

  val testUserId = "test-user-id"

  lateinit var notificationApply: Notification
  lateinit var notificationAccepted: Notification
  lateinit var notificationRejected: Notification
  lateinit var notificationComment: Notification
  lateinit var notificationReply: Notification

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    notificationService = NotificationService(notificationRepository)

    notificationApply = Notification(
      notificationType = NotificationType.APPLY,
      targetUserId = testUserId,
    )

    notificationAccepted = Notification(
      notificationType = NotificationType.ACCEPTED,
      targetUserId = testUserId,
    )

    notificationRejected = Notification(
      notificationType = NotificationType.REJECTED,
      targetUserId = testUserId,
    )

    notificationComment = Notification(
      notificationType = NotificationType.COMMENT,
      targetUserId = testUserId,
    )

    notificationReply = Notification(
      notificationType = NotificationType.REPLY,
      targetUserId = testUserId,
    )
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("createNotification") {
    context("type 과 targetUserId 가 주어지면") {
      it("저장 후 알림을 반환한다") {
        runTest {
          for (type in NotificationType.entries) {
            val notification = Notification(
              notificationType = type,
              targetUserId = testUserId,
            )

            every { notificationRepository.save(any()) } answers { Mono.just(notification) }

            val result = notificationService.createNotification(type, testUserId)

            result.notificationType shouldBe type
            result.isRead shouldBe false
            result.targetUserId shouldBe testUserId
          }
        }
      }
    }
  }

})

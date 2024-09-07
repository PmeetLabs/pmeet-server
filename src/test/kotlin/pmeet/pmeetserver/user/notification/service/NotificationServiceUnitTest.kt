package pmeet.pmeetserver.user.notification.service

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.springframework.http.codec.ServerSentEvent
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.repository.notification.NotificationRepository
import pmeet.pmeetserver.user.service.notification.NotificationService
import reactor.core.publisher.Flux
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

            coEvery { notificationRepository.save(any()) } answers { Mono.just(notification) }
            coEvery { notificationRepository.emitNotification(any()) } just Runs

            val result = notificationService.createNotification(type, testUserId)

            result.notificationType shouldBe type
            result.isRead shouldBe false
            result.targetUserId shouldBe testUserId

            coVerify(exactly = 1) {
              notificationRepository.emitNotification(notification)
            }
          }
        }
      }
    }
  }


  describe("subscribeToUserNotifications") {
    context("userId 가 주어지면") {
      it("ServerSentEvents 를 반환한다.") {
        runTest {
          val existingNotifications = listOf(
            Notification(notificationType = NotificationType.COMMENT, targetUserId = testUserId),
            Notification(notificationType = NotificationType.REPLY, targetUserId = testUserId)
          )
          val newNotification = Notification(notificationType = NotificationType.COMMENT, targetUserId = testUserId)

          every { notificationRepository.findAllByTargetUserIdAndIsReadFalse(testUserId) } returns Flux.fromIterable(
            existingNotifications
          )
          every { notificationRepository.subscribeToUserNotifications(testUserId) } returns Flux.just(
            ServerSentEvent.builder(
              newNotification
            ).build()
          )

          val resultFlow = notificationService.subscribeToUserNotifications(testUserId)

          val collectedEvents = withTimeout(5000) {
            resultFlow.take(3).toList()
          }
          collectedEvents.size shouldBe 3
          collectedEvents[0].data()?.notificationType shouldBe NotificationType.COMMENT
          collectedEvents[1].data()?.notificationType shouldBe NotificationType.REPLY
          collectedEvents[2].data()?.notificationType shouldBe NotificationType.COMMENT

          verify {
            notificationRepository.findAllByTargetUserIdAndIsReadFalse(testUserId)
            notificationRepository.subscribeToUserNotifications(testUserId)
          }
        }
      }
    }
  }

  describe("markNotificationAsRead") {
    context("알림 id 가 주어지면") {
      it("해당 id 를 읽음 처리한다") {
        runTest {
          val notification =
            Notification(id = "test-id", notificationType = NotificationType.COMMENT, targetUserId = testUserId)


          coEvery { notificationRepository.findById("test-id") } answers { Mono.just(notification) }

          notification.isRead = true

          coEvery { notificationRepository.save(any()) } answers { Mono.just(notification) }

          val result = notificationService.markNotificationAsRead("test-id")

          result.isRead shouldBe true
          result.id shouldBe "test-id"

          coVerify(exactly = 1) {
            notificationRepository.findById("test-id")
            notificationRepository.save(any())
          }
        }
      }
    }
  }

  describe("getUnreadNotificationsCount") {
    context("userId 가 주어지면") {
      it("해당 사용자의 읽지 않은 알림 개수를 반환한다.") {
        runTest {
          coEvery { notificationRepository.findAllByTargetUserIdAndIsReadFalse(testUserId) } returns Flux.fromIterable(
            mutableListOf(
              notificationApply,
              notificationReply,
              notificationComment,
              notificationAccepted,
              notificationRejected
            )
          )

          val result = notificationService.getUnreadNotificationsCount(testUserId)

          result shouldBe 5

          coVerify(exactly = 1) { notificationRepository.findAllByTargetUserIdAndIsReadFalse(testUserId) }
        }
      }
    }
  }

})

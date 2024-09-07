package pmeet.pmeetserver.user.notification.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.TestSecurityConfig
import pmeet.pmeetserver.user.controller.NotificationController
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.service.notification.NotificationService

@WebFluxTest(NotificationController::class)
@Import(TestSecurityConfig::class)
internal class NotificationControllerUnitTest : DescribeSpec() {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  lateinit var notificationService: NotificationService

  init {
    val userId = "test-user-id"
    val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
    describe("GET api/v1/notifications/subscribe/{userId}") {
      context("유저의 알림 구독 요청이 들어오면") {
        it("알림을 전송한다") {
          val notification1 = Notification(id = "1", notificationType = NotificationType.COMMENT, targetUserId = userId)
          val notification2 = Notification(id = "2", notificationType = NotificationType.REPLY, targetUserId = userId)

          coEvery { notificationService.subscribeToUserNotifications(userId) } returns flowOf(
            ServerSentEvent.builder(notification1).build(),
            ServerSentEvent.builder(notification2).build()
          )

          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .get()
            .uri("/api/v1/notifications/subscribe/$userId")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().valueMatches("Content-Type", "text/event-stream(;charset=UTF-8)?")
            .returnResult(String::class.java)
            .responseBody
            .take(2)
            .collectList()
            .block()

          coVerify { notificationService.subscribeToUserNotifications(userId) }
        }
      }
    }

    describe("PUT api/v1/notifications/{id}/read") {
      context("유저의 알림 읽음 처리 요청이 들어오면") {
        val notificationId = "test-notification-id"
        val updatedNotification = Notification(
          id = notificationId,
          notificationType = NotificationType.COMMENT,
          targetUserId = "user-id",
          isRead = true
        )

        coEvery { notificationService.markNotificationAsRead(notificationId) } returns updatedNotification

        val performedRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/notifications/$notificationId/read")
          .exchange()

        it("알림 읽음 처리 로직이 수행된다") {
          coVerify(exactly = 1) { notificationService.markNotificationAsRead(notificationId) }
        }

        it("요청은 성공한다") {
          performedRequest.expectStatus().isOk
        }

        it("응답으로 오는 알림은 읽음으로 반환된다") {
          performedRequest.expectBody<Notification>().consumeWith { result ->
            val returnedNotification = result.responseBody!!

            returnedNotification.id shouldBe notificationId
            returnedNotification.isRead shouldBe true
          }
        }
      }
    }

    describe("GET /api/v1/notifications/unread-count/{userId}") {
      context("유저의 읽지 않은 알림 개수 조회 요청이 들어오면") {
        val userId = "test-user-id"
        val unreadCount = 5

        coEvery { notificationService.getUnreadNotificationsCount(userId) } returns unreadCount

        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .get()
          .uri("/api/v1/notifications/unread-count/$userId")
          .exchange()

        it("알림의 개수를 확인하는 메서드가 수행된다") {
          coVerify { notificationService.getUnreadNotificationsCount(userId) }
        }

        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("알림 개수는 읽지 않은 개수와 동일한다.") {
          performRequest.expectBody<Int>().consumeWith { result ->
            val returnedNotification = result.responseBody!!

            returnedNotification shouldBe unreadCount
          }
        }
      }
    }
  }

}


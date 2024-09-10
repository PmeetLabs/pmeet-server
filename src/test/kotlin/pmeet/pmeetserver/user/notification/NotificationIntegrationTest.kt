package pmeet.pmeetserver.user.notification

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import pmeet.pmeetserver.config.BaseMongoDBTestForIntegration
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.enum.NotificationType
import pmeet.pmeetserver.user.domain.notification.Notification
import pmeet.pmeetserver.user.repository.UserRepository
import pmeet.pmeetserver.user.repository.notification.NotificationRepository
import pmeet.pmeetserver.user.service.notification.NotificationService

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExperimentalCoroutinesApi
@ActiveProfiles("test")
class NotificationIntegrationTest : BaseMongoDBTestForIntegration() {

  override fun isolationMode(): IsolationMode? {
    return IsolationMode.InstancePerLeaf
  }

  @Autowired
  private lateinit var notificationService: NotificationService

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var notificationRepository: NotificationRepository

  lateinit var user: User
  var userId: String = "test-user-id"

  override suspend fun beforeSpec(spec: Spec) {
    user = User(
      id = userId,
      email = "testEmail@test.com",
      name = "testName",
      nickname = "nickname",
      phoneNumber = "phone",
      gender = Gender.MALE,
      profileImageUrl = "image-url"
    )

    withContext(Dispatchers.IO) {
      userRepository.save(user).block()
    }
  }

  override suspend fun afterSpec(spec: Spec) {
    withContext(Dispatchers.IO) {
      userRepository.deleteAll().block()
      notificationRepository.deleteAll().block()
    }
  }

  init {
    val mockAuthentication = UsernamePasswordAuthenticationToken(userId, null, null)
    describe("GET api/v1/notifications/subscribe/{userId}") {

      notificationRepository.save(
        Notification(
          notificationType = NotificationType.APPLY,
          targetUserId = userId,
        )
      ).block()

      notificationRepository.save(
        Notification(
          notificationType = NotificationType.ACCEPTED,
          targetUserId = userId,
        )
      ).block()

      notificationRepository.save(
        Notification(
          notificationType = NotificationType.REJECTED,
          targetUserId = userId,
        )
      ).block()

      notificationRepository.save(
        Notification(
          notificationType = NotificationType.COMMENT,
          targetUserId = userId,
        )
      ).block()

      notificationRepository.save(
        Notification(
          notificationType = NotificationType.REPLY,
          targetUserId = userId,
        )
      ).block()

      context("유저의 알림 구독 요청이 들어오면") {
        it("알림을 전송한다") {

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
            .take(5)
            .collectList()
            .block()
        }
      }
    }

    describe("PUT api/v1/notifications/{id}/read") {
      context("유저의 알림 읽음 처리 요청이 들어오면") {
        val notification = notificationRepository.save(
          Notification(
            notificationType = NotificationType.APPLY,
            targetUserId = userId,
          )
        ).block()

        val notificationId = notification?.id

        val performedRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .put()
          .uri("/api/v1/notifications/$notificationId/read")
          .exchange()

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

        notificationRepository.save(
          Notification(
            notificationType = NotificationType.APPLY,
            targetUserId = userId,
          )
        ).block()

        notificationRepository.save(
          Notification(
            notificationType = NotificationType.ACCEPTED,
            targetUserId = userId,
          )
        ).block()

        notificationRepository.save(
          Notification(
            notificationType = NotificationType.REJECTED,
            targetUserId = userId,
          )
        ).block()

        notificationRepository.save(
          Notification(
            notificationType = NotificationType.COMMENT,
            targetUserId = userId,
          )
        ).block()

        notificationRepository.save(
          Notification(
            notificationType = NotificationType.REPLY,
            targetUserId = userId,
          )
        ).block()

        val performRequest = webTestClient
          .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
          .get()
          .uri("/api/v1/notifications/unread-count/$userId")
          .exchange()


        it("요청은 성공한다") {
          performRequest.expectStatus().isOk
        }

        it("알림 개수는 읽지 않은 개수와 동일한다.") {
          performRequest.expectBody<Int>().consumeWith { result ->
            val returnedNotification = result.responseBody!!

            returnedNotification shouldBe 5
          }
        }
      }
    }

    describe("PUT /api/v1/notifications/read/all/{userId}") {
      context("유저의 모든 알림 읽음 처리 요청이 들어오면") {
        beforeTest {
          notificationRepository.deleteAll().block()
          listOf(NotificationType.APPLY, NotificationType.ACCEPTED, NotificationType.REJECTED).forEach {
            notificationRepository.save(
              Notification(
                notificationType = it,
                targetUserId = userId,
              )
            ).block()
          }
        }

        it("모든 알림을 읽음 처리하고 업데이트된 알림 목록을 반환한다") {
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .put()
            .uri("/api/v1/notifications/read/all/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<Notification>>()
            .consumeWith { result ->
              val notifications = result.responseBody!!
              notifications.size shouldBe 3
              notifications.all { it.isRead } shouldBe true
            }
        }
      }
    }

    describe("DELETE /api/v1/notifications/{id}/delete") {
      context("특정 알림 삭제 요청이 들어오면") {
        lateinit var notificationId: String

        beforeTest {
          val notification = notificationRepository.save(
            Notification(
              notificationType = NotificationType.APPLY,
              targetUserId = userId,
            )
          ).block()
          notificationId = notification!!.id!!
        }

        it("해당 알림을 삭제한다") {
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .delete()
            .uri("/api/v1/notifications/$notificationId/delete")
            .exchange()
            .expectStatus().isOk

          // Verify the notification is deleted
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .get()
            .uri("/api/v1/notifications/unread-count/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody<Int>()
            .isEqualTo(0)
        }
      }
    }

    describe("DELETE /api/v1/notifications/delete/all/{userId}") {
      context("유저의 모든 알림 삭제 요청이 들어오면") {
        beforeTest {
          notificationRepository.deleteAll().block()
          listOf(NotificationType.APPLY, NotificationType.ACCEPTED, NotificationType.REJECTED).forEach {
            notificationRepository.save(
              Notification(
                notificationType = it,
                targetUserId = userId,
              )
            ).block()
          }
        }

        it("해당 유저의 모든 알림을 삭제한다") {
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .delete()
            .uri("/api/v1/notifications/delete/all/$userId")
            .exchange()
            .expectStatus().isOk

          // Verify all notifications are deleted
          webTestClient
            .mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuthentication))
            .get()
            .uri("/api/v1/notifications/unread-count/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody<Int>()
            .isEqualTo(0)
        }
      }
    }
  }
}

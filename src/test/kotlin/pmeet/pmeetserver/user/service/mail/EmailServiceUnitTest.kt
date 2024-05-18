package pmeet.pmeetserver.user.service.mail

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import reactor.core.publisher.Mono
import java.time.Duration

@ExperimentalCoroutinesApi
internal class EmailServiceUnitTest : DescribeSpec({

  lateinit var javaMailSender: JavaMailSender
  lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
  lateinit var emailService: EmailService

  val testDispatcher = StandardTestDispatcher()

  beforeContainer {
    Dispatchers.setMain(testDispatcher)
  }

  beforeTest {
    javaMailSender = mockk(relaxed = true)
    reactiveRedisTemplate = mockk(relaxed = true)

    emailService = EmailService(javaMailSender, reactiveRedisTemplate, testDispatcher)
  }

  afterContainer {
    Dispatchers.resetMain()
  }

  describe("sendEmailWithVerificationCode") {
    context("정상적으로 전송되면") {
      it("Redis에 인증 코드가 저장된다") {
        runTest {
          val email = "test@test.com"

          every {
            reactiveRedisTemplate.opsForValue().set(any<String>(), any<String>(), any<Duration>())
          } answers { Mono.just(true) }

          emailService.sendEmailWithVerificationCode(email)

          verify(exactly = 1) { javaMailSender.send(any<MimeMessage>()) }
          verify(exactly = 1) { reactiveRedisTemplate.opsForValue().set(any<String>(), any<String>(), any<Duration>()) }

        }
      }
    }
  }

  describe("verifyVerificationCode") {
    context("올바른 인증 코드가 주어지면") {
      it("검증에 성공한다") {
        runTest {
          val email = "test@test.com"
          val correctCode = "123456"

          every { reactiveRedisTemplate.opsForValue().get(email) } answers { Mono.just(correctCode) }

          val result = emailService.verifyVerificationCode(email, correctCode)

          result shouldBe true
        }
      }
    }

    context("잘못된 인증 코드가 주어지면") {
      it("UnauthorizedException 예외를 던진다") {
        runTest {
          val email = "test@test.com"
          val wrongCode = "654321"

          every { reactiveRedisTemplate.opsForValue().get(email) } answers { Mono.just("123456") }

          val exception = shouldThrow<UnauthorizedException> {
            emailService.verifyVerificationCode(email, wrongCode)
          }

          exception.errorCode shouldBe ErrorCode.VERIFICATION_CODE_NOT_MATCH
        }
      }
    }
  }

  describe("validateVerifiedEmail") {
    context("이메일 인증되었다면") {
      it("예외를 던지지 않는다") {
        runTest {
          val email = "test@test.com"

          every { reactiveRedisTemplate.opsForValue().get("${email}_verified") } answers { Mono.just("123456") }

          shouldNotThrowAny {
            emailService.validateVerifiedEmail(email)
          }
        }
      }
    }

    context("이에일 인증이 되지 않았다면") {
      it("UnauthorizedException 예외를 던진다") {
        runTest {
          val email = "test@test.com"

          every { reactiveRedisTemplate.opsForValue().get("${email}_verified") } answers { Mono.empty() }

          val exception = shouldThrow<UnauthorizedException> {
            emailService.validateVerifiedEmail(email)
          }

          exception.errorCode shouldBe ErrorCode.NOT_VERIFIED_EMAIL
        }
      }
    }
  }
})


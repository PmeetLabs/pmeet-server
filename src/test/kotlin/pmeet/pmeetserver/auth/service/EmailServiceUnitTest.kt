package pmeet.pmeetserver.auth.service

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
class EmailServiceUnitTest : DescribeSpec({

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

  describe("이메일 인증 코드 전송") {
    it("이메일 인증 코드가 정상적으로 전송된다.") {
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

  describe("인증 코드 검증") {
    it("올바른 인증 코드가 제공되면 검증에 성공해야 한다") {
      runTest {
        val email = "test@test.com"
        val correctCode = "123456"

        every { reactiveRedisTemplate.opsForValue().get(email) } answers { Mono.just(correctCode) }

        val result = emailService.verifyVerificationCode(email, correctCode)

        result shouldBe true
      }
    }

    it("잘못된 인증 코드가 제공되면 UnauthorizedException이 발생해야 한다") {
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

  describe("이메일 인증 상태 확인") {
    it("이메일이 인증되었다면 예외가 발생하지 않는다") {
      runTest {
        val email = "test@test.com"

        every { reactiveRedisTemplate.opsForValue().get("${email}_verified") } answers { Mono.just("123456") }

        shouldNotThrowAny {
          emailService.validateVerifiedEmail(email)
        }
      }
    }

    it("이메일이 인증되지 않았다면 UnauthorizedException이 발생해야 한다") {
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
})


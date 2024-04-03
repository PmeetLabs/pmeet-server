package pmeet.pmeetserver.auth.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.common.generator.VerificationCodeGenerator
import java.time.Duration

@Service
class EmailService(
  private val javaMailSender: JavaMailSender,
  private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>,
  private val dispatcher: CoroutineDispatcher
) {

  companion object {
    private val VERIFICATION_CODE_TIME_TO_LIVE by lazy { 5L }
    private val VERIFICATION_CONFIRMED_TIME_TO_LIVE by lazy { 5L }
    private val SUBJECT_MESSAGE by lazy { "pmeet 회원가입 인증 번호" }
    private val EMAIL_BODY_TEMPLATE by lazy {
      """
            <div style="font-family: Arial, '맑은 고딕', sans-serif; color: #333;">
                <h2>회원가입 인증 코드</h2>
                <p><b>pmeet</b>에 등록해 주셔서 감사합니다. 다음 코드를 사용하여 등록을 완료해 주세요:</p>
                <p><b style="font-size: 24px; color: #555;">{verificationCode}</b></p>
                <p>이 코드는 <b>${VERIFICATION_CODE_TIME_TO_LIVE}분 동안</b> 유효합니다.</p>
                <p>본인이 요청하지 않았다면, 이 이메일을 무시해 주세요.</p>
            </div>
        """.trimIndent()
    }
  }

  @Transactional
  suspend fun sendEmailWithVerificationCode(email: String) {
    withContext(dispatcher) {
      val message = javaMailSender.createMimeMessage()
      val verificationCode = VerificationCodeGenerator.generateVerificationCode()

      MimeMessageHelper(message, true, "UTF-8").apply {
        setTo(email)
        setSubject(SUBJECT_MESSAGE)
        setText(EMAIL_BODY_TEMPLATE.replace("{verificationCode}", verificationCode), true)
      }

      javaMailSender.send(message)

      reactiveRedisTemplate.opsForValue()
        .set(email, verificationCode, Duration.ofMinutes(VERIFICATION_CODE_TIME_TO_LIVE)).subscribe()
    }
  }

  @Transactional
  suspend fun verifyVerificationCode(email: String, verificationCode: String): Boolean {
    reactiveRedisTemplate.opsForValue().get(email).awaitSingleOrNull()?.also { storedCode ->
      when {
        storedCode != verificationCode -> {
          throw UnauthorizedException(ErrorCode.VERIFICATION_CODE_NOT_MATCH)
        }
      }
    } ?: throw UnauthorizedException(ErrorCode.VERIFICATION_CODE_EXPIRED)

    reactiveRedisTemplate.opsForValue()
      .set(
        email + "_verified",
        VerificationCodeGenerator.generateVerificationCode(),
        Duration.ofMinutes(VERIFICATION_CONFIRMED_TIME_TO_LIVE)
      ).subscribe()
    return true
  }

  @Transactional
  suspend fun validateVerifiedEmail(email: String) {
    println(email + "_verified")
    reactiveRedisTemplate.opsForValue().get(email + "_verified").awaitSingleOrNull()
      ?: throw UnauthorizedException(ErrorCode.NOT_VERIFIED_EMAIL)
  }
}

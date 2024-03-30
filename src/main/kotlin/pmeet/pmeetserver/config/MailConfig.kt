package pmeet.pmeetserver.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl


@Configuration
class MailConfig(
  @Value("\${spring.mail.host}") val host: String,
  @Value("\${spring.mail.port}") val port: Int,
  @Value("\${spring.mail.username}") val username: String,
  @Value("\${spring.mail.password}") val password: String,
  @Value("\${spring.mail.default-encoding}") val defaultEncoding: String,
  @Value("\${spring.mail.properties.mail.smtp.auth}") val smtpAuth: Boolean,
  @Value("\${spring.mail.properties.mail.smtp.ssl.enable}") val sslEnable: Boolean,
  @Value("\${spring.mail.properties.mail.smtp.connectiontimeout}") val connectionTimeout: Int,
  @Value("\${spring.mail.properties.mail.smtp.starttls.enable}") val startTlsEnable: Boolean,
  @Value("\${spring.mail.properties.mail.smtp.timeout}") val timeout: Int,
  @Value("\${spring.mail.properties.mail.smtp.writtentimeout}") val writtenTimeout: Int,
  @Value("\${spring.mail.properties.mail.auth-code-expiration-millis}") val authCodeExpirationMillis: Long
) {

  @Bean
  fun javaMailSender(): JavaMailSender = JavaMailSenderImpl().apply {
    this.host = this@MailConfig.host
    this.port = this@MailConfig.port
    this.username = this@MailConfig.username
    this.password = this@MailConfig.password
    this.defaultEncoding = this@MailConfig.defaultEncoding
    javaMailProperties.apply {
      put("mail.smtp.auth", smtpAuth.toString())
      put("mail.smtp.ssl.enable", sslEnable.toString())
      put("mail.smtp.connectiontimeout", connectionTimeout)
      put("mail.smtp.starttls.enable", startTlsEnable.toString())
      put("mail.smtp.timeout", timeout)
      put("mail.smtp.writtentimeout", writtenTimeout)
      put("mail.auth-code-expiration-millis", authCodeExpirationMillis)
    }
  }
}

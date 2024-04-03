package pmeet.pmeetserver.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider

@Configuration
class AwsS3Config(
  @Value("\${amazon.aws.access-key}") private val accessKey: String,
  @Value("\${amazon.aws.secret-key}") private val secretKey: String
) {

  @Bean
  fun awsCredentials(): AwsCredentials {
    return AwsBasicCredentials.create(accessKey, secretKey)
  }

  @Bean
  fun credentialsProvider(): AwsCredentialsProvider {
    return AwsCredentialsProvider { awsCredentials() }
  }
}

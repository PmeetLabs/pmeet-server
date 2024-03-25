package pmeet.pmeetserver.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
  @Value("\${spring.data.redis.host}") val host: String,
  @Value("\${spring.data.redis.port}") val port: Int
) {

  @Primary
  @Bean
  fun connectionFactory(): ReactiveRedisConnectionFactory? {
    return LettuceConnectionFactory(host, port)
  }

  @Bean
  fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
    val context = RedisSerializationContext.newSerializationContext<String, String>()
      .key(StringRedisSerializer())
      .hashKey(StringRedisSerializer())
      .value(StringRedisSerializer())
      .hashValue(StringRedisSerializer())
      .build()
    return ReactiveRedisTemplate(factory, context)
  }
}

package pmeet.pmeetserver.config

import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.ReactiveMongoTemplate


@Configuration
class MongoTestConfig {

  @Autowired
  private lateinit var environment: Environment

  @Bean("testMongoTemplate")
  fun mongoTemplateConfig(): ReactiveMongoTemplate {
    val mongoUri = environment.getRequiredProperty("spring.data.mongodb.uri", String::class.java)
    val mongoClient = MongoClients.create(mongoUri)
    return ReactiveMongoTemplate(mongoClient, "test")
  }
}

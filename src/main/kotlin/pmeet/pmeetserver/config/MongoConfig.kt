package pmeet.pmeetserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableReactiveMongoRepositories(basePackages = ["pmeet.pmeetserver"])
class MongoConfig {

  @Bean
  fun transactionManager(reactiveMongoDatabaseFactory: ReactiveMongoDatabaseFactory): ReactiveMongoTransactionManager {
    return ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory)
  }

  @Bean
  fun transactionalOperator(reactiveTransactionManager: ReactiveMongoTransactionManager): TransactionalOperator {
    return TransactionalOperator.create(reactiveTransactionManager)
  }

}

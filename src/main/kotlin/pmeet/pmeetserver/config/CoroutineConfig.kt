package pmeet.pmeetserver.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {

  @Bean
  fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

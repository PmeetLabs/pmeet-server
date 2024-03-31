package pmeet.pmeetserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder

@Configuration
class WebClientConfig {

  @Bean
  fun webClientBuilder(): WebClient.Builder {
    return WebClient.builder()
  }

  @Bean
  fun webClient(webClientBuilder: Builder): WebClient {
    return webClientBuilder.build()
  }
}

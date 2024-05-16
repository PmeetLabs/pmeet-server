package pmeet.pmeetserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class TestSecurityConfig {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http {
      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      authorizeExchange {
        authorize("/api/v1/auth/**", permitAll)
        authorize(anyExchange, authenticated)
      }
    }
  }
}

package pmeet.pmeetserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@Configuration
class SecurityConfig {
  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean
  fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http {
      cors { disable() }
      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      authorizeExchange {
        authorize("/v3/api-docs/**", permitAll)
        authorize("/webjars/**", permitAll)
        authorize("/api/v1/auth/**", permitAll)
        authorize(anyExchange, authenticated)
      }
    }
  }
}

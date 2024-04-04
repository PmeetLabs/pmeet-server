package pmeet.pmeetserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@EnableWebFluxSecurity
@Configuration
class SecurityConfig {
  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  open fun authApiFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http {
      cors { configurationSource = corsConfigurationSource() }
      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      securityMatcher(PathPatternParserServerWebExchangeMatcher("/api/v1/auth/**"))
      authorizeExchange {
        authorize("/api/v1/auth/**", permitAll)
      }
    }
  }

  @Bean
  fun filterChain(
    http: ServerHttpSecurity,
    converter: JwtServerAuthenticationConverter,
    handler: ServerAuthenticationFailureHandler,
    authManager: JwtAuthenticationManager
  ): SecurityWebFilterChain {
    val filter = AuthenticationWebFilter(authManager)
    filter.setServerAuthenticationConverter(converter)
    filter.setAuthenticationFailureHandler(handler)

    return http {
      cors { configurationSource = corsConfigurationSource() }
      csrf { disable() }
      formLogin { disable() }
      httpBasic { disable() }
      authorizeExchange {
        authorize(pathMatchers("/v3/api-docs/**", "/webjars/**"), permitAll)
        authorize(anyExchange, authenticated)
      }
      addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
    }
  }

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource {
    val configuration = CorsConfiguration()
    // TODO: set allowed origin
    configuration.allowedOriginPatterns = listOf("*")
    configuration.allowedHeaders = listOf("*")
    configuration.allowedMethods = listOf("*")
    configuration.allowCredentials = true
    configuration.maxAge = 3600L
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", configuration)
    return source
  }
}

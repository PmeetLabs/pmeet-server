package pmeet.pmeetserver.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.slf4j.LoggerFactory
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebSession


@Configuration
class SpringDocConfig {
  private val logger = LoggerFactory.getLogger(SpringDocConfig::class.java)

  init {
    SpringDocUtils.getConfig().addRequestWrapperToIgnore(
      WebSession::class.java,
    )
  }

  @Bean
  fun openApi(): OpenAPI {
    logger.debug("Starting Swagger")

    return OpenAPI()
      .info(
        Info()
          .title("pmeet rest api")
          .version("v1")
          .description("REST API")
      )
      .components(
        Components().addSecuritySchemes(
          "bearerAuth",
          SecurityScheme().type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        )
      )
      .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
  }
}

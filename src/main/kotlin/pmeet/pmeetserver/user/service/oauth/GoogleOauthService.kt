package pmeet.pmeetserver.user.service.oauth

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class GoogleOauthService(
  private val webClient: WebClient,
  @Value("\${google.oauth.client-id}") private val clientId: String,
  @Value("\${google.oauth.client-secret}") private val clientSecret: String,
  @Value("\${google.oauth.redirect-uri}") private val redirectUri: String
) {
  suspend fun getIdToken(code: String): String {
    val responseBody: Map<String, Any> = webClient.post()
      .uri("https://oauth2.googleapis.com/token")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData("code", code)
        .with("client_id", clientId)
        .with("client_secret", clientSecret)
        .with("redirect_uri", redirectUri)
        .with("grant_type", "authorization_code"))
      .retrieve()
      .bodyToMono<Map<String, Any>>()
      .awaitSingle()
    return responseBody["id_token"] as String
  }
}

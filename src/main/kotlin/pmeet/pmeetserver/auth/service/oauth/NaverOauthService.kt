package pmeet.pmeetserver.auth.service.oauth

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class NaverOauthService(

  private val webClient: WebClient,
  @Value("\${naver.oauth.client-id}") private val clientId: String,
  @Value("\${naver.oauth.client-secret}") private val clientSecret: String
) {
  suspend fun getAccessToken(code: String, state: String): String {
    val responseBody: Map<String, Any> = webClient.post()
      .uri("https://nid.naver.com/oauth2.0/token/")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData("code", code)
        .with("client_id", clientId)
        .with("client_secret", clientSecret)
        .with("state", state)
        .with("grant_type", "authorization_code"))
      .retrieve()
      .bodyToMono<Map<String, Any>>()
      .awaitSingle()
    return responseBody["access_token"].toString()
  }

  suspend fun getProfile(accessToken: String): UserInfo {
    val NaverResponse = webClient.post()
      .uri("https://openapi.naver.com/v1/nid/me")
      .header("Authorization", "Bearer " + accessToken)
      .retrieve()
      .bodyToMono(NaverResponse::class.java)
      .awaitSingle()
    return UserInfo(NaverResponse.response.name, NaverResponse.response.email)
  }
}

data class NaverResponse(
  val resultcode: String,
  val message: String,
  val response: Response
)

data class Response(
  val id: String,
  val email: String,
  val name: String
)

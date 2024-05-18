package pmeet.pmeetserver.user.service.oauth

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import pmeet.pmeetserver.user.dto.UserInfo

@Service
class KakaoOauthService(

  private val webClient: WebClient,
  @Value("\${kakao.oauth.client-id}") private val clientId: String,
  @Value("\${kakao.oauth.redirect-uri}") private val redirectUri: String
) {
  suspend fun getAccessToken(code: String): String {
    val responseBody: Map<String, Any> = webClient.post()
      .uri("https://kauth.kakao.com/oauth/token")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(BodyInserters.fromFormData("code", code)
        .with("client_id", clientId)
        .with("redirect_uri", redirectUri)
        .with("grant_type", "authorization_code"))
      .retrieve()
      .bodyToMono<Map<String, Any>>()
      .awaitSingle()
    return responseBody["access_token"].toString()
  }

  suspend fun getProfile(accessToken: String): UserInfo {
    val kakaoResponse = webClient.post()
      .uri("https://kapi.kakao.com/v2/user/me")
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .header("Authorization", "Bearer " + accessToken)
      .retrieve()
      .bodyToMono(KakaoResponse::class.java)
      .awaitSingle()
    return UserInfo(kakaoResponse.kakao_account.profile.nickname, kakaoResponse.kakao_account.email)
  }
}

data class KakaoResponse(
  val kakao_account: KakaoAccount
)

data class KakaoAccount(
  val email: String,
  val profile: KakaoProfile
)

data class KakaoProfile(
  val nickname: String
)

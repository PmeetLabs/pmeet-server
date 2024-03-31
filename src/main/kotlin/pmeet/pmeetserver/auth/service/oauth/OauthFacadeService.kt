package pmeet.pmeetserver.auth.service.oauth

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.service.UserService

@Service
class OauthFacadeService(
  private val googleAuthService: GoogleOauthService,
  private val naverAuthService: NaverOauthService,
  private val kakaoAuthService: KakaoOauthService,
  private val userService: UserService
) {
  suspend fun loginGoogleOauth(code: String): UserResponseDto {
    val idToken = googleAuthService.getIdToken(code)
    val userInfo = decodeIdToken(idToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      // TODO JWT 반환으로 변경
      return UserResponseDto.from(user)
    } else {
      // TODO nickName 부분 변경, JWT 반환
      val savedUser = userService.save(User(email = userInfo.email, name = userInfo.name, provider = "google", nickname = "google"))
      return UserResponseDto.from(savedUser)
    }
  }

  suspend fun loginNaverOauth(code: String, state: String): UserResponseDto {
    val accessToken = naverAuthService.getAccessToken(code, state)
    val userInfo = naverAuthService.getProfile(accessToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      // TODO JWT 반환으로 변경
      return UserResponseDto.from(user)
    } else {
      // TODO nickName 부분 변경, JWT 반환
      val savedUser = userService.save(User(email = userInfo.email, name = userInfo.name, provider = "naver", nickname = "naver"))
      return UserResponseDto.from(savedUser)
    }
  }

  suspend fun loginKakaoOauth(code: String): UserResponseDto {
    val accessToken = kakaoAuthService.getAccessToken(code)
    val userInfo = kakaoAuthService.getProfile(accessToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      // TODO JWT 반환으로 변경
      return UserResponseDto.from(user)
    } else {
      // TODO nickName 부분 변경, JWT 반환
      val savedUser = userService.save(User(email = userInfo.email, name = userInfo.name, provider = "kakao", nickname = "kakao"))
      return UserResponseDto.from(savedUser)
    }
  }

  fun decodeIdToken(idToken: String): UserInfo {
    val decodedJWT: DecodedJWT = JWT.decode(idToken)
    val name = decodedJWT.getClaim("name").asString()
    val email = decodedJWT.getClaim("email").asString()
    return UserInfo(name, email)
  }
}

data class UserInfo(
  val name: String,
  val email: String
)

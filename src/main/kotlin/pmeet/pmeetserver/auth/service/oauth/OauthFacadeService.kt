package pmeet.pmeetserver.auth.service.oauth

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import pmeet.pmeetserver.common.utils.jwt.JwtUtil
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.UserInfo
import pmeet.pmeetserver.user.dto.response.UserJwtDto
import pmeet.pmeetserver.user.service.UserService

@Service
class OauthFacadeService(
  private val googleAuthService: GoogleOauthService,
  private val naverAuthService: NaverOauthService,
  private val kakaoAuthService: KakaoOauthService,
  private val userService: UserService,
  private val jwtUtil: JwtUtil
) {
  suspend fun loginGoogleOauth(code: String): UserJwtDto {
    val idToken = googleAuthService.getIdToken(code)
    val userInfo = decodeIdToken(idToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      return jwtUtil.createToken(user.id!!)
    } else {
      // TODO 동시성 문제 해결 추후 도입
      val nickNameNumber = generateNicknameNumber()
      val nickname = "Pmeet#${String.format("%04d", nickNameNumber)}"
      val savedUser = userService.save(User(
        email = userInfo.email,
        name = userInfo.name,
        provider = "google",
        nickname = nickname,
        nicknameNumber = nickNameNumber))
      return jwtUtil.createToken(savedUser.id!!)
    }
  }

  suspend fun loginNaverOauth(code: String, state: String): UserJwtDto {
    val accessToken = naverAuthService.getAccessToken(code, state)
    val userInfo = naverAuthService.getProfile(accessToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      return jwtUtil.createToken(user.id!!)
    } else {
      // TODO 동시성 문제 해결 추후 도입
      val nickNameNumber = generateNicknameNumber()
      val nickname = "Pmeet#${String.format("%04d", nickNameNumber)}"
      val savedUser = userService.save(User(
        email = userInfo.email,
        name = userInfo.name,
        provider = "naver",
        nickname = nickname,
        nicknameNumber = nickNameNumber))
      return jwtUtil.createToken(savedUser.id!!)
    }
  }

  suspend fun loginKakaoOauth(code: String): UserJwtDto {
    val accessToken = kakaoAuthService.getAccessToken(code)
    val userInfo = kakaoAuthService.getProfile(accessToken)

    val user = userService.findUserByEmail(userInfo.email)
    if (user != null) {
      return jwtUtil.createToken(user.id!!)
    } else {
      // TODO 동시성 문제 해결 추후 도입
      val nickNameNumber = generateNicknameNumber()
      val nickname = "Pmeet#${String.format("%04d", nickNameNumber)}"
      val savedUser = userService.save(User(
        email = userInfo.email,
        name = userInfo.name,
        provider = "kakao",
        nickname = nickname,
        nicknameNumber = nickNameNumber))
      return jwtUtil.createToken(savedUser.id!!)
    }
  }

  private suspend fun decodeIdToken(idToken: String): UserInfo {
    val decodedJWT: DecodedJWT = JWT.decode(idToken)
    val name = decodedJWT.getClaim("name").asString()
    val email = decodedJWT.getClaim("email").asString()
    return UserInfo(name, email)
  }

  private suspend fun generateNicknameNumber(): Int {
    val highestNicknameNumber = userService.findUserWithHighestNicknameNumber()?.nicknameNumber ?: 0
    return highestNicknameNumber + 1
  }
}

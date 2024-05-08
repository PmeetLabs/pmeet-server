package pmeet.pmeetserver.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.auth.service.EmailService
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.common.utils.jwt.JwtUtil
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.dto.request.CheckMailRequestDto
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.dto.request.SendVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.request.SetPasswordRequestDto
import pmeet.pmeetserver.user.dto.request.SignInRequestDto
import pmeet.pmeetserver.user.dto.request.SignUpRequestDto
import pmeet.pmeetserver.user.dto.request.VerifyVerificationCodeRequestDto
import pmeet.pmeetserver.user.dto.response.UserJwtDto

@ExperimentalCoroutinesApi
internal class UserFacadeServiceUnitTest : DescribeSpec({
  isolationMode = IsolationMode.InstancePerTest

  val testDispatcher = StandardTestDispatcher()

  lateinit var userFacadeService: UserFacadeService
  val passwordEncoder = mockk<PasswordEncoder>(relaxed = true)
  val userService = mockk<UserService>(relaxed = true)
  val emailService = mockk<EmailService>(relaxed = true)
  val jwtUtil = mockk<JwtUtil>(relaxed = true)

  lateinit var user: User

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    userFacadeService = UserFacadeService(
      passwordEncoder,
      userService,
      emailService,
      jwtUtil
    )

    user = User(
      email = "testEmail@test.com",
      name = "testName",
      password = "testPassword",
      nickname = "testNickname"
    )
    ReflectionTestUtils.setField(user, "id", "testId")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("signUpRequestDto가 주어지면") {
      val signUpRequestDto = SignUpRequestDto(
        "testEmail@test.com",
        "testName",
        "testPassword",
        "testNickname"
      )
      it("저장 후 UserResponseDto 반환") {
        runTest {
          val encodedPassword = "encodedPassword"

          coEvery { emailService.validateVerifiedEmail(signUpRequestDto.email) } coAnswers { }
          every { passwordEncoder.encode(signUpRequestDto.password) } returns encodedPassword
          coEvery { userService.save(any()) } returns user

          val result = userFacadeService.save(signUpRequestDto)

          result.email shouldBe signUpRequestDto.email
          result.name shouldBe signUpRequestDto.name
          result.nickname shouldBe signUpRequestDto.nickname
        }
      }
    }
  }

  describe("signIn") {
    context("signInRequestDto가 주어지면") {
      val signInRequestDto = SignInRequestDto("testEmail@test.com", "testPassword")

      it("인증 후 토큰 발급") {
        runTest {
          val userJwtDto = UserJwtDto("testId", "accessToken", "refreshToken")

          coEvery { userService.getUserByEmail(signInRequestDto.email) } returns user
          every { passwordEncoder.matches(signInRequestDto.password, user.password) } returns true
          coEvery { jwtUtil.createToken(user.id!!) } returns userJwtDto

          val result = userFacadeService.signIn(signInRequestDto)

          result.userId shouldBe user.id
          result.accessToken shouldBe "accessToken"
          result.refreshToken shouldBe "refreshToken"
        }
      }

      it("패스워드 불일치인 경우 Exception") {
        runTest {
          coEvery { userService.getUserByEmail(signInRequestDto.email) } returns user
          every { passwordEncoder.matches(signInRequestDto.password, user.password) } returns false

          shouldThrow<UnauthorizedException> {
            userFacadeService.signIn(signInRequestDto)
          }.errorCode shouldBe ErrorCode.INVALID_PASSWORD
        }
      }
    }
  }

  describe("isDuplicateNickName") {
    context("CheckNickNameRequestDto가 주어지면") {
      val checkNickNameRequestDto = CheckNickNameRequestDto("testNickname")
      it("중복 닉네임이 있는 경우") {
        runTest {
          coEvery { userService.findUserByNickname(checkNickNameRequestDto.nickname) } returns user

          val result = userFacadeService.isDuplicateNickName(checkNickNameRequestDto)

          result shouldBe true
        }
      }
      it("중복 닉네임이 없는 경우") {
        coEvery { userService.findUserByNickname(checkNickNameRequestDto.nickname) } returns null

        val result = userFacadeService.isDuplicateNickName(checkNickNameRequestDto)

        result shouldBe false
      }
    }
  }

  describe("isDuplicateMail") {
    context("CheckMailRequestDto가 주어지면") {
      val checkMailRequestDto = CheckMailRequestDto("testEmail@test.com")
      it("중복 이메일이 있는 경우") {
        runTest {
          coEvery { userService.findUserByEmail(checkMailRequestDto.mail) } returns user

          val result = userFacadeService.isDuplicateMail(checkMailRequestDto)

          result shouldBe true
        }
      }
      it("중복 이메일이 없는 경우") {
        coEvery { userService.findUserByEmail(checkMailRequestDto.mail) } returns null

        val result = userFacadeService.isDuplicateMail(checkMailRequestDto)

        result shouldBe false
      }
    }
  }

  describe("sendVerificationCode") {
    context("SendVerificationCodeRequestDto가 주어지면") {
      val sendVerificationCodeRequestDto = SendVerificationCodeRequestDto("testEmail@test.com")
      it("코드가 정상적으로 전송된 경우") {
        coEvery { emailService.sendEmailWithVerificationCode(sendVerificationCodeRequestDto.email) } just Runs

        val result = userFacadeService.sendVerificationCode(sendVerificationCodeRequestDto)

        result shouldBe true
      }
    }
  }

  describe("verifyVerificationCode") {
    context("VerifyVerificationCodeRequestDto가 주어지면") {
      val verifyVerificationCodeRequestDto = VerifyVerificationCodeRequestDto("testEmail@test.com",
        "correctCode")

      it("인증 코드와 일치한 경우") {
        coEvery {
          emailService.verifyVerificationCode(verifyVerificationCodeRequestDto.email,
            verifyVerificationCodeRequestDto.verificationCode)
        } returns true

        val result = userFacadeService.verifyVerificationCode(verifyVerificationCodeRequestDto)

        result shouldBe true
      }
    }
  }

  describe("setPassword") {
    context("SetPasswordRequestDto가 주어지면") {
      val setPasswordRequestDto = SetPasswordRequestDto(
        "testEmail@test.com",
        "newPassword",
        "newPassword"
      )
      it("패스워드 변경 후 저장") {
        runTest {
          coEvery { userService.getUserByEmail(setPasswordRequestDto.email) } returns user
          coEvery { emailService.validateVerifiedEmail(setPasswordRequestDto.email) } just Runs
          every { passwordEncoder.encode(setPasswordRequestDto.password) } returns "encodedNewPassword"
          coEvery { userService.update(any()) } returns user

          val result = userFacadeService.setPassword(setPasswordRequestDto)

          result shouldBe true
        }
      }
    }
  }

  describe("getMySummaryInfo") {
    context("유저 ID가 주어지면") {
      val userId = "testId"
      it("유저를 조회한 후 UserSummaryResponseDto 반환") {
        runTest {
          coEvery { userService.getUserById(userId) } returns user

          val result = userFacadeService.getMySummaryInfo(userId)

          result.id shouldBe user.id
          result.email shouldBe user.email
          result.nickname shouldBe user.nickname
        }
      }
    }
  }

})

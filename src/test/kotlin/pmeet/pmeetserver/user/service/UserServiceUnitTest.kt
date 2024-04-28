package pmeet.pmeetserver.user.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.repository.UserRepository
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
internal class UserServiceUnitTest : DescribeSpec({
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  lateinit var userService: UserService
  val userRepository = mockk<UserRepository>(relaxed = true)

  lateinit var user: User

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    userService = UserService(userRepository)

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
    context("유저 정보가 주어지면") {
      it("저장 후 유저 반환") {
        runTest {

          every { userRepository.save((any())) } answers { Mono.just(user) }
          every { userRepository.findByEmail(user.email) } answers { Mono.empty() }
          every { userRepository.findByNickname(user.nickname) } answers { Mono.empty() }

          val result = userService.save(user)

          result shouldBe user
        }
      }

      it("이미 존재하는 메일의 경우 Exception") {
        runTest {
          every { userRepository.findByEmail(user.email) } answers { Mono.just(user) }

          val exception = shouldThrow<EntityDuplicateException> {
            userService.save(user)
          }

          exception.errorCode shouldBe ErrorCode.USER_DUPLICATE_BY_EMAIL
        }
      }

      it("이미 존재하는 닉네임의 경우 Exception") {
        runTest {
          every { userRepository.findByEmail(user.email) } answers { Mono.empty() }
          every { userRepository.findByNickname(user.nickname) } answers { Mono.just(user) }

          val exception = shouldThrow<EntityDuplicateException> {
            userService.save(user)
          }

          exception.errorCode shouldBe ErrorCode.USER_DUPLICATE_BY_NICKNAME
        }
      }
    }
  }

  describe("getUserByNickname") {
    context("닉네임이 주어지면") {
      it("유저 반환") {
        runTest {
          every { userRepository.findByNickname(user.nickname) } answers { Mono.just(user) }

          val result = userService.getUserByNickname(user.nickname)

          result shouldBe user
        }
      }
      it("존재하는 유저가 없을 경우 Exception") {
        runTest {
          every { userRepository.findByNickname(user.nickname) } answers { Mono.empty() }

          val exception = shouldThrow<EntityNotFoundException> {
            userService.getUserByNickname(user.nickname)
          }

          exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND_BY_NICKNAME
        }
      }
    }
  }

  describe("getUserByEmail") {
    context("이메일이 주어지면") {
      it("유저 반환") {
        runTest {
          every { userRepository.findByEmail(user.email) } answers { Mono.just(user) }

          val result = userService.getUserByEmail(user.email)

          result shouldBe user
        }
      }

      it("존재하지 않는 이메일의 경우 Exception") {
        runTest {
          every { userRepository.findByEmail(any()) } answers { Mono.empty() }

          val exception = shouldThrow<EntityNotFoundException> {
            userService.getUserByEmail(user.email)
          }

          exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND_BY_EMAIL
        }
      }
    }
  }

  describe("getUserById") {
    context("사용자 ID가 주어지면") {
      it("유저 반환") {
        runTest {
          every { userRepository.findById(user.id!!) } answers { Mono.just(user) }

          val result = userService.getUserById(user.id!!)

          result shouldBe user
        }
      }

      it("존재하지 않는 ID의 경우 Exception") {
        runTest {
          every { userRepository.findById(user.id!!) } answers { Mono.empty() }

          val exception = shouldThrow<EntityNotFoundException> {
            userService.getUserById(user.id!!)
          }

          exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND_BY_ID
        }
      }
    }
  }

  // Meaningless Test?
  describe("update") {
    context("유저 정보 업데이트") {
      it("업데이트 후 유저 반환") {
        runTest {
          val modifiedUser = User(
            email = "testEmail@test.com",
            name = "testName",
            password = "testPassword",
            nickname = "modifiedNickname",
          )

          every { userRepository.save(modifiedUser) } answers { Mono.just(modifiedUser) }

          val result = userService.update(modifiedUser)

          result shouldBe modifiedUser
        }
      }
    }
  }

  describe("findUserWithHighestNicknameNumber") {
    context("가장 높은 닉네임 번호를 가진 유저 찾기") {
      it("유저 반환") {
        runTest {
          every { userRepository.findTopByOrderByNicknameNumberDesc() } answers { Mono.just(user) }

          val result = userService.findUserWithHighestNicknameNumber()

          result shouldBe user
        }
      }

      it("유저가 존재하지 않을 경우 null 반환") {
        runTest {
          every { userRepository.findTopByOrderByNicknameNumberDesc() } answers { Mono.empty() }

          val result = userService.findUserWithHighestNicknameNumber()

          result shouldBe null
        }
      }
    }
  }
})


package pmeet.pmeetserver.user.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import pmeet.pmeetserver.config.BaseMongoDBTestForRepository
import pmeet.pmeetserver.user.domain.User

//@Transactional
@ExperimentalCoroutinesApi
internal class UserRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : BaseMongoDBTestForRepository({
  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val userRepository = factory.getRepository(UserRepository::class.java)

  lateinit var user: User

  beforeSpec {
    user = User(
      email = "testEmail@test.com",
      name = "testName",
      password = "testPassword",
      nickname = "testNickname",
      nicknameNumber = 1
    )
    userRepository.save(user).block()

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    userRepository.deleteAll().block()
  }

  describe("findByEmail") {
    context("이메일이 주어지면") {
      it("유저 반환") {
        runTest {

          val result = userRepository.findByEmailAndIsDeletedFalse(user.email).block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }

    context("탈퇴한 회원의 이메일이 주어지면") {
      val deletedUser = User(
        email = "deleted@email.com",
        name = "deletedName",
        password = "deletedPassword",
        nickname = "deletedNickname",
        isDeleted = true
      )
      it("null 반환") {
        runTest {

          val result = userRepository.findByEmailAndIsDeletedFalse(deletedUser.email).block()

          result shouldBe null
        }
      }
    }
  }

  describe("findByNickname") {
    context("닉네임이 주어지면") {
      it("유저 반환") {
        runTest {

          val result = userRepository.findByNicknameAndIsDeletedFalse(user.nickname).block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
    context("탈퇴한 회원의 닉네임이 주어지면") {
      val deletedUser = User(
        email = "deleted@email.com",
        name = "deletedName",
        password = "deletedPassword",
        nickname = "deletedNickname",
        isDeleted = true
      )
      it("null 반환") {
        runTest {

          val result = userRepository.findByNicknameAndIsDeletedFalse(deletedUser.nickname).block()

          result shouldBe null
        }
      }
    }
  }

  describe("findTopByOrderByNicknameNumberDesc") {
    context("가장 큰 닉네임 숫자를 가진 유저를 반환할 때") {
      it("유저 반환") {
        runTest {

          val result = userRepository.findFirstByIsDeletedFalseOrderByNicknameNumberDesc().block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
    context("탈퇴한 회원이 가장 큰 닉네임 숫자를 가진 유저일 때") {
      val deletedUser = User(
        email = "deleted@email.com",
        name = "deletedName",
        password = "deletedPassword",
        nickname = "deletedNickname",
        isDeleted = true,
        nicknameNumber = 9999999
      )
      it("탈퇴한 회원을 반환하지 않음") {
        runTest {

          val result = userRepository.findFirstByIsDeletedFalseOrderByNicknameNumberDesc().block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
  }

})

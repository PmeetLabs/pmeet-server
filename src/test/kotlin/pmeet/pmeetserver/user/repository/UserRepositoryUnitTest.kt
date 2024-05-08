import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.config.MongoTestConfig
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.repository.UserRepository

//@Transactional
@ExperimentalCoroutinesApi
@ContextConfiguration(classes = [MongoTestConfig::class])
internal class UserRepositoryUnitTest(
  @Autowired @Qualifier("testMongoTemplate") private val template: ReactiveMongoTemplate
) : DescribeSpec({
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

          val result = userRepository.findByEmail(user.email).block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
  }

  describe("findByNickname") {
    context("닉네임이 주어지면") {
      it("유저 반환") {
        runTest {

          val result = userRepository.findByNickname(user.nickname).block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
  }

  describe("findTopByOrderByNicknameNumberDesc") {
    context("가장 큰 닉네임 숫자를 가진 유저를 반환할 때") {
      it("유저 반환") {
        runTest {

          val result = userRepository.findTopByOrderByNicknameNumberDesc().block()

          result?.email shouldBe user.email
          result?.name shouldBe user.name
          result?.nickname shouldBe user.nickname
          result?.password shouldBe user.password
        }
      }
    }
  }

}) {
  companion object {
    @Container
    val mongoDBContainer = MongoDBContainer("mongo:latest").apply {
      withExposedPorts(27017)
      start()
    }

    init {
      System.setProperty(
        "spring.data.mongodb.uri",
        "mongodb://localhost:${mongoDBContainer.getMappedPort(27017)}/test"
      )
    }
  }
}

package pmeet.pmeetserver.user.techStack.repository

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
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.techStack.CustomTechStackRepositoryImpl
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository

@ExperimentalCoroutinesApi
@DataMongoTest
internal class TechStackRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val customTechStackRepository = CustomTechStackRepositoryImpl(template)
  val techStackRepository = factory.getRepository(TechStackRepository::class.java, customTechStackRepository)

  lateinit var techStack: TechStack

  beforeSpec {
    techStack = TechStack(
      name = "testName",
    )
    techStackRepository.save(techStack).block()

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    techStackRepository.deleteAll().block()
  }

  describe("findByName") {
    context("기술 스택 이름이 주어지면") {
      it("해당하는 이름의 기술 스택을 반환한다") {
        runTest {

          val result = techStackRepository.findByName(techStack.name).block()

          result?.name shouldBe techStack.name
        }
      }
    }
  }

  describe("findByNameSearchSlice") {
    context("기술 스택 이름과 페이징 정보가 주어지면") {
      val name = "testName"
      val pageNumber = 0
      val pageSize = 10
      for (i in 1..pageSize) {
        techStackRepository.save(TechStack(name = name + i)).block()
      }
      it("이름을 포함하는 기술 스택들은 이름 오름차순, 이름 길이 오름차순으로 반환한다") {
        runTest {
          val result =
            techStackRepository.findByNameSearchSlice(name, PageRequest.of(pageNumber, pageSize)).collectList().block()

          result?.size shouldBe pageSize + 1
          result?.first()?.name shouldBe name
          result?.last()?.name shouldBe name + pageSize
        }
      }
    }

    context("기술 스택 이름이 주어지지 않으면") {
      val name = "testName"
      for (i in 1..10) {
        techStackRepository.save(TechStack(name = name + (11 - i))).block()
      }
      it("모든 기술 스택들을 이름 오름차순, 이름 길이 오름차순으로 반환한다") {
        runTest {
          val result = techStackRepository.findByNameSearchSlice(null, PageRequest.of(0, 10)).collectList().block()

          result?.size shouldBe 11
          result?.first()?.name shouldBe name
          result?.last()?.name shouldBe name + 10
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


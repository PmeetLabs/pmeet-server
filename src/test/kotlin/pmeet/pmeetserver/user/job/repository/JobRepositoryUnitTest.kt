package pmeet.pmeetserver.user.job.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import pmeet.pmeetserver.config.BaseMongoDBTestForRepository
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.repository.job.CustomJobRepositoryImpl
import pmeet.pmeetserver.user.repository.job.JobRepository

@ExperimentalCoroutinesApi
internal class JobRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : BaseMongoDBTestForRepository({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val customJobRepository = CustomJobRepositoryImpl(template)
  val jobRepository = factory.getRepository(JobRepository::class.java, customJobRepository)

  lateinit var job: Job

  beforeSpec {
    job = Job(
      name = "testName",
    )
    jobRepository.save(job).block()

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    jobRepository.deleteAll().block()
  }

  describe("findByName") {
    context("직무 이름이 주어지면") {
      it("해당하는 이름의 직무를 반환한다") {
        runTest {

          val result = jobRepository.findByName(job.name).block()

          result?.name shouldBe job.name
        }
      }
    }
  }

  describe("findByNameSearchSlice") {
    context("직무 이름과 페이징 정보가 주어지면") {
      val name = "testName"
      val pageNumber = 0
      val pageSize = 10
      for (i in 1..pageSize) {
        jobRepository.save(Job(name = name + i)).block()
      }
      val capitalName = "ABCDEFGH"
      jobRepository.save(Job(name = capitalName)).block()

      it("이름을 포함하는 직무들을 이름 오름차순, 이름 길이 오름차순으로 반환한다") {
        runTest {
          val result =
            jobRepository.findByNameSearchSlice(name, PageRequest.of(pageNumber, pageSize)).collectList().block()

          result?.size shouldBe pageSize + 1
          result?.first()?.name shouldBe name
          result?.last()?.name shouldBe name + pageSize
        }
      }
      it("직무 이름 검색에는 대소문자 구분 없이 검색된 결과를 반환한다") {
        runTest {
          val result =
            jobRepository.findByNameSearchSlice("abc", PageRequest.of(pageNumber, pageSize)).collectList().block()

          result?.size shouldBe 1
          result?.first()?.name shouldBe capitalName
        }
      }
    }

    context("직무 이름이 주어지지 않으면") {
      val name = "testName"
      for (i in 1..10) {
        jobRepository.save(Job(name = name + (11 - i))).block()
      }
      it("모든 직무들을 이름 오름차순, 이름 길이 오름차순으로 반환한다") {
        runTest {
          val result = jobRepository.findByNameSearchSlice(null, PageRequest.of(0, 10)).collectList().block()

          result?.size shouldBe 11
          result?.first()?.name shouldBe name
          result?.last()?.name shouldBe name + 10
        }
      }
    }
  }

})


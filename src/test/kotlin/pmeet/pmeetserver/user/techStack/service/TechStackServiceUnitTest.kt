package pmeet.pmeetserver.user.techStack.service

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
import org.springframework.data.domain.PageRequest
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import pmeet.pmeetserver.user.service.techStack.TechStackService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
internal class TechStackServiceUnitTest : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val techStackRepository = mockk<TechStackRepository>(relaxed = true)

  lateinit var techStackService: TechStackService
  lateinit var techStack: TechStack

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    techStackService = TechStackService(techStackRepository)

    techStack = TechStack(
      name = "testName",
    )
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("save") {
    context("기술 스택 정보가 주어지면") {
      it("저장 후 기술 스택을 반환한다") {
        runTest {
          every { techStackRepository.save(any()) } answers { Mono.just(techStack) }
          every { techStackRepository.findByName(techStack.name) } answers { Mono.empty() }

          val result = techStackService.save(techStack)

          result.name shouldBe techStack.name
        }
      }
    }

    context("이미 존재하는 기술 스택 이름이 주어지면") {
      every { techStackRepository.findByName(techStack.name) } answers { Mono.just(techStack) }
      it("EntityDuplicateException을 던진다") {
        runTest {
          val exception = shouldThrow<EntityDuplicateException> {
            techStackService.save(techStack)
          }

          exception.errorCode shouldBe ErrorCode.TECHSTACK_DUPLICATE_BY_NAME
        }
      }
    }
  }

  describe("searchBytechStackName") {
    context("기술 스택 이름과 페이징 정보가 주어지면") {
      val name = "Test"
      val pageNumber = 0
      val pageSize = 10
      val techStacks = mutableListOf<TechStack>()
      for (i in 1..pageSize * 2) {
        techStacks.add(TechStack(name = name + i))
      }

      it("이름을 포함하는 기술 스택들을 Slice로 반환한다") {
        runTest {
          every { techStackRepository.findByNameSearchSlice(name, any()) } answers {
            Flux.fromIterable(
              techStacks.subList(
                0,
                pageSize + 1
              )
            )
          }

          val result = techStackService.searchByTechStackName(name, PageRequest.of(pageNumber, pageSize))

          result.size shouldBe pageSize
          result.isFirst shouldBe true
          result.isLast shouldBe false
          result.content.size shouldBe pageSize
          result.content.first().name shouldBe name + 1
          result.content.last().name shouldBe name + pageSize
          result.hasNext() shouldBe true
        }
      }
    }
  }

})

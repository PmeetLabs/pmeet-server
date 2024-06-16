package pmeet.pmeetserver.user.techStack.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.techStack.request.CreateTechStackRequestDto
import pmeet.pmeetserver.user.service.techStack.TechStackFacadeService
import pmeet.pmeetserver.user.service.techStack.TechStackService

@ExperimentalCoroutinesApi
internal class TechStackFacadeServiceUnitTest : DescribeSpec({

  val testDispatcher = StandardTestDispatcher()

  val techStackService = mockk<TechStackService>(relaxed = true)

  lateinit var techStackFacadeService: TechStackFacadeService

  lateinit var techStack: TechStack

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    techStackFacadeService = TechStackFacadeService(techStackService)

    techStack = TechStack(
      name = "testName",
    )
    ReflectionTestUtils.setField(techStack, "id", "testId")
  }

  afterSpec {
    Dispatchers.resetMain()
  }

  describe("cratedtechStack") {
    context("createtechStackRequestDto가 주어지면") {
      val requestDto = CreateTechStackRequestDto(
        name = "testName"
      )
      it("techStackResponseDto를 반환한다") {
        runTest {
          coEvery { techStackService.save(any()) } answers { techStack }
          val result = techStackFacadeService.createTechStack(requestDto)

          result.name shouldBe techStack.name
        }
      }
    }
  }

  describe("searchtechStackByName") {
    context("직무 이름과 페이징 정보가 주어지면") {
      val name = "Test"
      val pageNumber = 0
      val pageSize = 10
      val techStacks = mutableListOf<TechStack>()
      for (i in 1..pageSize * 2) {
        techStacks.add(TechStack(name = name + i))
        ReflectionTestUtils.setField(techStacks[i - 1], "id", "testId$i")
      }
      it("이름을 포함하는기술 스택들을 Slice로 반환한다") {
        runTest {
          coEvery { techStackService.searchByTechStackName(name, any()) } answers {
            SliceImpl(
              techStacks.subList(0, pageSize),
              PageRequest.of(pageNumber, pageSize),
              true
            )
          }
          val result = techStackFacadeService.searchTechStackByName(name, PageRequest.of(pageNumber, pageSize))

          result.size shouldBe pageSize
          result.isFirst shouldBe true
          result.isLast shouldBe false
          result.hasNext() shouldBe true
          result.forEachIndexed { index, techStackResponseDto ->
            techStackResponseDto.name shouldBe name + (index + 1)
          }
        }
      }
    }
  }

})

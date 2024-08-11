package pmeet.pmeetserver.project.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.test.util.ReflectionTestUtils
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.ProjectBookmark
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.enums.ProjectSortProperty
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
@DataMongoTest
internal class ProjectRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val customRepository = CustomProjectRepositoryImpl(template)
  val projectRepository = factory.getRepository(ProjectRepository::class.java, customRepository)

  lateinit var userId: String

  beforeSpec {
    Dispatchers.setMain(testDispatcher)
    userId = "testUserId"
  }

  afterSpec {
    projectRepository.deleteAll().block()
    Dispatchers.resetMain()
  }

  describe("findAllByFilter") {
    context("완료 여부가 False이고, 필터가 주어지지 않으면") {
      it("완료되지 않은 Project를 대상으로 PageSize + 1만큼 잘라서 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.name))
        ).collectList().block()

        result?.size shouldBe 11
      }
    }
    context("완료 여부가 True이고, 필터가 주어지지 않으면") {
      it("완료된 Project를 대상으로 PageSize + 1만큼 잘라서 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i",
            isCompleted = true
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          true,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.name))
        ).collectList().block()

        result?.size shouldBe 11
      }
    }
    context("ALL 타입 필터가 주어지면") {
      val filterType = ProjectFilterType.ALL
      val filterValue = "2"
      it("Title 또는 JobName에 filterValue가 포함된 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          filterType,
          filterValue,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.name))
        ).collectList().block()

        result?.size shouldBe 3
      }
    }
    context("TITLE 타입 필터가 주어지면") {
      val filterType = ProjectFilterType.TITLE
      val filterValue = "Title10"
      it("Title에 filterValue가 포함된 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          filterType,
          filterValue,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.name))
        ).collectList().block()

        result?.size shouldBe 1
        result?.first()?.title shouldBe "testTitle10"
      }
    }
    context("JOB_NAME 타입 필터가 주어지면") {
      val filterType = ProjectFilterType.JOB_NAME
      val filterValue = "JobName10"
      it("JobName에 filterValue가 포함된 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          filterType,
          filterValue,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.name))
        ).collectList().block()

        result?.size shouldBe 1
        result?.first()?.title shouldBe "testTitle10"
      }
    }
    context("북마크수 내림차순 정렬이 주어지면") {
      it("북마크수 내림차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          for (j in 1..i) {
            project.bookmarkers.add(ProjectBookmark("testUserId$j", LocalDateTime.now()))
          }
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.BOOK_MARKERS.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle20"
        result?.last()?.title shouldBe "testTitle10"
      }
    }
    context("북마크수 오름차순 정렬이 주어지면") {
      it("북마크수 오름차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          for (j in 1..i) {
            project.bookmarkers.add(ProjectBookmark("testUserId$j", LocalDateTime.now()))
          }
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, ProjectSortProperty.BOOK_MARKERS.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle1"
        result?.last()?.title shouldBe "testTitle11"
      }
    }
    context("생성일 오름차순 정렬이 주어지면") {
      it("생성일 오름차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          ReflectionTestUtils.setField(
            project,
            "createdAt",
            LocalDateTime.of(2024, 7, 21, 0, 0, 0).plusDays(i.toLong())
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, ProjectSortProperty.CREATED_AT.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle1"
        result?.last()?.title shouldBe "testTitle11"
      }
    }
    context("생성일 내림차순 정렬이 주어지면") {
      it("생성일 내림차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          ReflectionTestUtils.setField(
            project,
            "createdAt",
            LocalDateTime.of(2024, 7, 21, 0, 0, 0).plusDays(i.toLong())
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.CREATED_AT.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle20"
        result?.last()?.title shouldBe "testTitle10"
      }
    }
    context("수정일 오름차순 정렬이 주어지면") {
      it("수정일 오름차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          ReflectionTestUtils.setField(
            project,
            "updatedAt",
            LocalDateTime.of(2024, 7, 21, 0, 0, 0).plusDays(i.toLong())
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, ProjectSortProperty.UPDATED_AT.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle1"
        result?.last()?.title shouldBe "testTitle11"
      }
    }
    context("수정일 내림차순 정렬이 주어지면") {
      it("수정일 내림차순으로 Project를 반환한다") {
        for (i in 1..20) {
          val project = Project(
            userId = userId,
            title = "testTitle$i",
            startDate = LocalDateTime.of(2024, 7, 21, 0, 0, 0),
            endDate = LocalDateTime.of(2024, 7, 22, 0, 0, 0),
            recruitments = listOf(
              Recruitment(
                jobName = "testJobName$i",
                numberOfRecruitment = 1
              )
            ),
            description = "testDescription$i"
          )
          ReflectionTestUtils.setField(
            project,
            "updatedAt",
            LocalDateTime.of(2024, 7, 21, 0, 0, 0).plusDays(i.toLong())
          )
          projectRepository.save(project).block()
        }
        val result = projectRepository.findAllByFilter(
          false,
          null,
          null,
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, ProjectSortProperty.UPDATED_AT.property))
        ).collectList().block()

        result?.size shouldBe 11
        result?.first()?.title shouldBe "testTitle20"
        result?.last()?.title shouldBe "testTitle10"
      }
    }
  }

})

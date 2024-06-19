package pmeet.pmeetserver.user.resume.repository

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
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.Gender
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.job.CustomJobRepositoryImpl
import pmeet.pmeetserver.user.repository.job.JobRepository
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.repository.techStack.CustomTechStackRepositoryImpl
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import java.time.LocalDate

@ExperimentalCoroutinesApi
@ContextConfiguration(classes = [MongoTestConfig::class])
internal class ResumeRepositoryUnitTest(
  @Autowired @Qualifier("testMongoTemplate") private val template: ReactiveMongoTemplate
) : DescribeSpec({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val customJobRepository = CustomJobRepositoryImpl(template)
  val jobRepository = factory.getRepository(JobRepository::class.java, customJobRepository)
  val customTechStackRepository = CustomTechStackRepositoryImpl(template)
  val techStackRepository = factory.getRepository(TechStackRepository::class.java, customTechStackRepository)

  val resumeRepository = factory.getRepository(ResumeRepository::class.java)

  lateinit var job: Job
  lateinit var techStack: TechStack
  lateinit var jobExperience: JobExperience
  lateinit var projectExperience: ProjectExperience
  lateinit var resume: Resume

  beforeSpec {
    job = Job(
      name = "testName",
    )
    jobRepository.save(job).block()

    techStack = TechStack(
      name = "testName",
    )
    techStackRepository.save(techStack).block()

    jobExperience = JobExperience(
      companyName = "companyName",
      experiencePeriod = ExperienceYear.YEAR_01,
      responsibilities = "jobExperienceResponsibility",
    )

    projectExperience = ProjectExperience(
      projectName = "projectName",
      experiencePeriod = ExperienceYear.YEAR_00,
      responsibilities = "projectExperienceResponsibility",
    )

    resume = Resume(
      title = "title",
      userName = "userName",
      userGender = Gender.MALE,
      userBirthDate = LocalDate.of(2024, 6, 20),
      userPhoneNumber = "010-1234-5678",
      userEmail = "userEmail@example.com",
      userProfileImageUrl = "userProfileImage.com",
      desiredJobs = mutableListOf(job),
      techStacks = mutableListOf(techStack),
      jobExperiences = mutableListOf(jobExperience),
      projectExperiences = mutableListOf(projectExperience),
      portfolioFileUrl = "portfolioFile.com",
      portfolioUrl = mutableListOf("portfolioUrl.com"),
      selfDescription = "self description",
    )

    val savedResume = resumeRepository.save(resume).block()

    resume = savedResume ?: resume

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    jobRepository.deleteAll().block()
    techStackRepository.deleteAll().block()
    resumeRepository.deleteAll().block()
  }

  describe("findById") {
    context("이력서 아이디가 주어지면") {
      it("해당하는 아이디의 이력서를 반환한다") {
        runTest {
          val result = resumeRepository.findById(resume.id ?: "").block()

          result?.title shouldBe resume.title
          result?.userName shouldBe resume.userName
          result?.userGender shouldBe resume.userGender
          result?.userBirthDate shouldBe resume.userBirthDate
          result?.userPhoneNumber shouldBe resume.userPhoneNumber
          result?.userEmail shouldBe resume.userEmail
          result?.userProfileImageUrl shouldBe resume.userProfileImageUrl
          result?.desiredJobs?.first?.name shouldBe resume.desiredJobs.first.name
          result?.techStacks?.first?.name shouldBe resume.techStacks.first.name
          result?.jobExperiences?.first?.companyName shouldBe resume.jobExperiences.first.companyName
          result?.projectExperiences?.first?.projectName shouldBe resume.projectExperiences.first.projectName
          result?.portfolioFileUrl shouldBe resume.portfolioFileUrl
          result?.portfolioUrl?.first shouldBe resume.portfolioUrl.first
          result?.selfDescription shouldBe resume.selfDescription
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
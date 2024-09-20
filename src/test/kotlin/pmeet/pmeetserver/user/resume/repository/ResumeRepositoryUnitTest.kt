package pmeet.pmeetserver.user.resume.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import pmeet.pmeetserver.project.domain.ProjectMember
import pmeet.pmeetserver.project.repository.ProjectMemberRepository
import pmeet.pmeetserver.user.domain.enum.ExperienceYear
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.job.Job
import pmeet.pmeetserver.user.domain.resume.JobExperience
import pmeet.pmeetserver.user.domain.resume.ProjectExperience
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.job.CustomJobRepositoryImpl
import pmeet.pmeetserver.user.repository.job.JobRepository
import pmeet.pmeetserver.user.repository.resume.CustomResumeRepositoryImpl
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.repository.techStack.CustomTechStackRepositoryImpl
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResume
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeList
import pmeet.pmeetserver.user.resume.ResumeGenerator.generateResumeListForSlice
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ResumeRepositoryUnitTest(
  @Autowired private val template: ReactiveMongoTemplate
) : BaseMongoDBTestForRepository({

  isolationMode = IsolationMode.InstancePerLeaf

  val testDispatcher = StandardTestDispatcher()

  val factory = ReactiveMongoRepositoryFactory(template)
  val customJobRepository = CustomJobRepositoryImpl(template)
  val jobRepository = factory.getRepository(JobRepository::class.java, customJobRepository)
  val customTechStackRepository = CustomTechStackRepositoryImpl(template)
  val techStackRepository = factory.getRepository(TechStackRepository::class.java, customTechStackRepository)

  val customResumeRepository = CustomResumeRepositoryImpl(template)

  val resumeRepository = factory.getRepository(ResumeRepository::class.java, customResumeRepository)
  val projectMemberRepository = factory.getRepository(ProjectMemberRepository::class.java)

  lateinit var job: Job
  lateinit var techStack: TechStack
  lateinit var jobExperience: JobExperience
  lateinit var projectExperience: ProjectExperience
  lateinit var resume: Resume
  lateinit var resumeList: List<Resume>
  lateinit var resumeListForSlice: List<Resume>
  lateinit var projectMemberList: MutableList<ProjectMember>

  val projectId = "test-project-id"

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

    resume = generateResume()

    val savedResume = resumeRepository.save(resume).block()

    resume = savedResume ?: resume

    resumeList = resumeRepository.saveAll(generateResumeList()).collectList().block().orEmpty()

    resumeListForSlice = resumeRepository.saveAll(generateResumeListForSlice()).collectList().block().orEmpty()

    projectMemberList = mutableListOf()
    for (resumes in resumeList) {
      val projectMember = ProjectMember(
        resumeId = resumes.id!!,
        tryoutId = "testTryout-" + resumes.id!!,
        userId = resumes.userId,
        userName = resumes.userName,
        userSelfDescription = resumes.selfDescription.orEmpty(),
        positionName = "testPositionName",
        createdAt = LocalDateTime.now(),
        projectId = projectId
      )
      projectMemberList.add(projectMember)
    }
    projectMemberRepository.saveAll(projectMemberList).collectList().block()

    Dispatchers.setMain(testDispatcher)
  }

  afterSpec {
    Dispatchers.resetMain()
    jobRepository.deleteAll().block()
    techStackRepository.deleteAll().block()
    resumeRepository.deleteAll().block()
    projectMemberRepository.deleteAll().block()
  }

  describe("findById") {
    context("이력서 아이디가 주어지면") {
      it("해당하는 아이디의 이력서를 반환한다") {
        runTest {
          val result = resumeRepository.findById(resume.id ?: "").block()


          result?.userName shouldBe resume.userName
          result?.userGender shouldBe resume.userGender
          result?.userBirthDate shouldBe resume.userBirthDate
          result?.userPhoneNumber shouldBe resume.userPhoneNumber
          result?.userEmail shouldBe resume.userEmail
          result?.userProfileImageUrl shouldBe resume.userProfileImageUrl
          result?.desiredJobs?.first()?.name shouldBe resume.desiredJobs.first().name
          result?.techStacks?.first()?.name shouldBe resume.techStacks.first().name
          result?.jobExperiences?.first()?.companyName shouldBe resume.jobExperiences.first().companyName
          result?.projectExperiences?.first()?.projectName shouldBe resume.projectExperiences.first().projectName
          result?.portfolioFileUrl shouldBe resume.portfolioFileUrl
          result?.portfolioUrl?.first() shouldBe resume.portfolioUrl.first()
          result?.selfDescription shouldBe resume.selfDescription
        }
      }
    }
  }

  describe("findByIdAndUserId") {
    context("이력서 아이디와 user id 가 주어지면") {
      it("해당하는 아이디의 이력서를 반환한다") {
        runTest {
          val result = resumeRepository.findByIdAndUserId(resume.id ?: "", resume.userId).block()

          result?.title shouldBe resume.title
          result?.userId shouldBe resume.userId
          result?.userName shouldBe resume.userName
          result?.userGender shouldBe resume.userGender
          result?.userBirthDate shouldBe resume.userBirthDate
          result?.userPhoneNumber shouldBe resume.userPhoneNumber
          result?.userEmail shouldBe resume.userEmail
          result?.userProfileImageUrl shouldBe resume.userProfileImageUrl
          result?.desiredJobs?.first()?.name shouldBe resume.desiredJobs.first().name
          result?.techStacks?.first()?.name shouldBe resume.techStacks.first().name
          result?.jobExperiences?.first()?.companyName shouldBe resume.jobExperiences.first().companyName
          result?.projectExperiences?.first()?.projectName shouldBe resume.projectExperiences.first().projectName
          result?.portfolioFileUrl shouldBe resume.portfolioFileUrl
          result?.portfolioUrl?.first() shouldBe resume.portfolioUrl.first()
          result?.selfDescription shouldBe resume.selfDescription
        }
      }
    }
  }

  describe("findAllByUserId") {
    context("user id 가 주어지면") {
      it("user의 이력서 목록을 반환한다") {
        runTest {
          val userId = "user2"
          val result = resumeRepository.findAllByUserId(userId).collectList().block()
          result?.size shouldBe resumeList.count { it.userId == userId }
        }
      }
    }
  }

  describe("findAllByIdIn") {
    context("resume id 목록이 주어지면") {
      it("해당 resume id 를 가진 resume 목록을 반환한다") {
        runTest {
          val result = resumeRepository.findAllByIdIn(resumeList.map { it.id!! }).collectList().block()
          result?.size shouldBe resumeList.size
        }
      }
    }
  }

  describe("findAllByFilter") {
    val userId = "testUserId"
    context("전체 검색이고, 검색어가 있으며 인기순 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.ALL,
            "1",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(12).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.ALL,
            "5",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 1
          resultWithTitle?.get(0)?.title shouldBe resumeListForSlice.get(4).title
        }
      }
    }

    context("전체 검색이고, 검색어가 없으며 인기순 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.ALL,
            "",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(12).title
        }
      }
    }

    context("전체 검색이고, 검색어가 있으며 최신 수정일 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.ALL,
            "title",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(0).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.ALL,
            "nickname1",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 5
          resultWithTitle?.get(0)?.title shouldBe resumeListForSlice.get(0).title
        }
      }
    }

    context("이력서 제목 검색이고, 검색어가 있으며 인기순 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.TITLE,
            "title",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(12).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.TITLE,
            "no_title",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 0
        }
      }
    }

    context("이력서 제목 검색이고, 검색어가 있으며 최신 수정일 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.TITLE,
            "title",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(0).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.TITLE,
            "no_title",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 0
        }
      }
    }

    context("직무 이름 검색이고, 검색어가 있으며 인기순 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.JOB,
            "job",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(12).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.JOB,
            "no_job",
            ResumeOrderType.POPULAR,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 0
        }
      }
    }

    context("넥네임 검색이고, 검색어가 있으며 인기순 조회인 경우") {
      it("조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.JOB,
            "job",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 9
          result?.get(0)?.title shouldBe resumeListForSlice.get(0).title

          val resultWithTitle = resumeRepository.findAllByFilter(
            userId,
            ResumeFilterType.JOB,
            "no_job",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          resultWithTitle?.size shouldBe 0
        }
      }
    }

    context("조회를 하는 경우에 ") {
      it("조회 요청자의 이력서는 제외하고 조건에 맞는 Resume 를 반환한다.") {
        runTest {
          val result = resumeRepository.findAllByFilter(
            "John-id",
            ResumeFilterType.TITLE,
            "title",
            ResumeOrderType.RECENT,
            PageRequest.of(0, 8)
          ).collectList().block()
          result?.size shouldBe 0
        }
      }
    }
  }

  describe("findProjectMembersWithResumeByProjectId") {
    context("프로젝트 ID가 주어지면") {
      it("해당 프로젝트의 멤버와 그에 연관된 이력서 정보를 함께 반환한다") {
        runTest {
          val result = resumeRepository.findProjectMembersWithResumeByProjectId(projectId)
            .collectList()
            .block()

          result shouldNotBe null
          result?.size shouldBe projectMemberList.size

          result?.forEach { projectMemberWithResume ->
            projectMemberWithResume.projectId shouldBe projectId

            // ProjectMember 정보 검증
            val expectedProjectMember = projectMemberList.find { it.userId == projectMemberWithResume.userId }
            projectMemberWithResume.resumeId shouldBe expectedProjectMember?.resumeId
            projectMemberWithResume.userName shouldBe expectedProjectMember?.userName
            projectMemberWithResume.userThumbnail shouldBe expectedProjectMember?.userThumbnail
            projectMemberWithResume.userSelfDescription shouldBe expectedProjectMember?.userSelfDescription
            projectMemberWithResume.positionName shouldBe expectedProjectMember?.positionName

            // Resume 정보 검증
            val expectedResume = listOf(resume) + resumeList
            val matchingResume = expectedResume.find { it.id == projectMemberWithResume.resumeId }
            projectMemberWithResume.resume.title shouldBe matchingResume?.title
            projectMemberWithResume.resume.userId shouldBe matchingResume?.userId
            projectMemberWithResume.resume.userName shouldBe matchingResume?.userName
          }
        }
      }
    }

    context("존재하지 않는 프로젝트 ID가 주어지면") {
      it("빈 리스트를 반환한다") {
        runTest {
          val result = resumeRepository.findProjectMembersWithResumeByProjectId("nonExistentProjectId")
            .collectList()
            .block()

          result?.size shouldBe 0
        }
      }
    }
  }
})

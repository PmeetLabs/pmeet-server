package pmeet.pmeetserver.project.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.springframework.test.util.ReflectionTestUtils
import pmeet.pmeetserver.project.domain.ProjectMember
import pmeet.pmeetserver.project.repository.ProjectMemberRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
internal class ProjectMemberServiceUnitTest : DescribeSpec({
    val testDispatcher = StandardTestDispatcher()

    val projectMemberRepository = mockk<ProjectMemberRepository>(relaxed = true)

    lateinit var projectMemberService: ProjectMemberService

    lateinit var projectMember: ProjectMember
    lateinit var userId: String
    lateinit var projectId: String
    lateinit var tryoutId: String

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        projectMemberService = ProjectMemberService(projectMemberRepository)

        userId = "testUserId"
        projectId = "testProjectId"
        tryoutId = "testTryoutId"

        projectMember = ProjectMember(
            resumeId = "testResumeId",
            tryoutId = tryoutId,
            userId = userId,
            userName = "testUserName",
            userSelfDescription = "selfDescription",
            positionName = "testPositionName",
            createdAt = LocalDateTime.now(),
            projectId = projectId
        )
        ReflectionTestUtils.setField(projectMember, "id", "testProjectMemberId")
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    describe("save") {
        context("프로젝트 멤버가 주어지면") {
            it("저장 후 프로젝트 멤버를 반환한다") {
                runTest {
                    every { projectMemberRepository.save(any()) } answers { Mono.just(projectMember) }

                    val result = projectMemberService.save(projectMember)

                    result.id shouldBe projectMember.id
                    result.resumeId shouldBe projectMember.resumeId
                    result.userId shouldBe projectMember.userId
                    result.userName shouldBe projectMember.userName
                    result.userSelfDescription shouldBe projectMember.userSelfDescription
                    result.positionName shouldBe projectMember.positionName
                    result.projectId shouldBe projectMember.projectId
                }
            }
        }
    }

    describe("deleteProjectMember") {
        context("프로젝트 멤버 삭제 요청이 주어지면") {
            it("프로젝트 멤버를 삭제한다") {
                runTest {
                    every { projectMemberRepository.deleteById(projectMember.id!!) } answers { Mono.empty() }

                    projectMemberService.deleteProjectMember(projectMember.id!!)

                    verify(exactly = 1) { projectMemberRepository.deleteById(projectMember.id!!) }
                }
            }
        }
    }

}) {
}
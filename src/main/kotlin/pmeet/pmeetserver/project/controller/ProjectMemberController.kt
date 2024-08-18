package pmeet.pmeetserver.project.controller

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pmeet.pmeetserver.project.service.ProjectFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/project-members")
class ProjectMemberController(private val projectFacadeService: ProjectFacadeService) {

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    suspend fun deleteProjectMember(
        @AuthenticationPrincipal userId: Mono<String>,
        @RequestParam("projectid") projectId: String,
        @RequestParam("memberid") memberId: String
    ) {
        projectFacadeService.deleteProjectMember(userId.awaitSingle(), projectId, memberId)
    }

}
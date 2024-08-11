package pmeet.pmeetserver.project.controller

import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.project.dto.request.tryout.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.request.tryout.ProjectTryoutResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/project-tryouts")
class ProjectTryoutController(
    private val projectFacadeService: ProjectFacadeService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createProjectTryout(
        @AuthenticationPrincipal userId: Mono<String>,
        @RequestBody @Valid requestDto: CreateProjectTryoutRequestDto
    ): ProjectTryoutResponseDto {
        return projectFacadeService.createProjectTryout(userId.awaitSingle(), requestDto)
    }

    @GetMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getProjectTryoutList(
        @AuthenticationPrincipal userId: Mono<String>,
        @PathVariable projectId: String
    ): List<ProjectTryoutResponseDto> {
        return projectFacadeService.getProjectTryoutListByProjectId(userId.awaitSingle(), projectId)
    }

}

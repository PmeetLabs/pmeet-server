package pmeet.pmeetserver.project.controller

import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import pmeet.pmeetserver.project.dto.tryout.request.CreateProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.request.PatchProjectTryoutRequestDto
import pmeet.pmeetserver.project.dto.tryout.response.ProjectTryoutResponseDto
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

  @PatchMapping("/accept")
  @ResponseStatus(HttpStatus.OK)
  suspend fun patchProjectTryoutToAccepted(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: PatchProjectTryoutRequestDto
  ): ProjectTryoutResponseDto {
    return projectFacadeService.patchProjectTryoutStatusToAccept(userId.awaitSingle(), requestDto)
  }

  @PatchMapping("/reject")
  @ResponseStatus(HttpStatus.OK)
  suspend fun patchProjectTryoutToRejected(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: PatchProjectTryoutRequestDto
  ): ProjectTryoutResponseDto {
    return projectFacadeService.pathProjectTryoutStatusToReject(userId.awaitSingle(), requestDto)
  }
}

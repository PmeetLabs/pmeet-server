package pmeet.pmeetserver.project.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
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
  @Operation(summary = "프밋 지원자 전체 목록 조회", description = "프밋 관리에서 조회되는 프밋 지원자의 전체 목록을 조회한다")
  suspend fun getProjectTryoutList(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ): List<ProjectTryoutResponseDto> {
    return projectFacadeService.getProjectTryoutListByProjectId(userId.awaitSingle(), projectId)
  }

  @GetMapping("/{projectId}/accept")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "프밋 참여자 전체 목록 조회", description = "완료 프밋 생성에서 조회되는 프밋 참여자의 전체 목록을 조회한다")
  suspend fun getAcceptedProjectTryoutList(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ): List<ProjectTryoutResponseDto> {
    return projectFacadeService.getAcceptedProjectTryoutListByProjectId(userId.awaitSingle(), projectId)
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

package pmeet.pmeetserver.project.controller

import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
  private val projectFacadeService: ProjectFacadeService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: CreateProjectRequestDto
  ): ProjectResponseDto {
    return projectFacadeService.createProject(userId.awaitSingle(), requestDto)
  }
}

package pmeet.pmeetserver.project.controller

import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.project.dto.request.comment.CreateProjectCommentRequestDto
import pmeet.pmeetserver.project.dto.request.comment.ProjectCommentResponseDto
import pmeet.pmeetserver.project.service.ProjectFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/project-comments")
class ProjectCommentController(
  private val projectFacadeService: ProjectFacadeService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createProjectComment(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: CreateProjectCommentRequestDto
  ): ProjectCommentResponseDto {
    return projectFacadeService.createProjectComment(userId.awaitSingle(), requestDto)
  }

  @DeleteMapping("/{commentId}")
  @ResponseStatus(HttpStatus.OK)
  suspend fun deleteProjectComment(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable commentId: String
  ): ProjectCommentResponseDto {
    return projectFacadeService.deleteProjectComment(userId.awaitSingle(), commentId)
  }
}

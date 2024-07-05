package pmeet.pmeetserver.user.controller

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.UpdateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/resumes")
class ResumeController(private val resumeFacadeService: ResumeFacadeService) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createResume(
    @RequestBody requestDto: CreateResumeRequestDto
  ): ResumeResponseDto {
    return resumeFacadeService.createResume(requestDto)
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  suspend fun getResumeById(
    @RequestParam(required = true) resumeId: String
  ): ResumeResponseDto {
    return resumeFacadeService.findResumeById(resumeId)
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  suspend fun updateResume(@AuthenticationPrincipal userId: Mono<String>, @RequestBody requestDto: UpdateResumeRequestDto): ResumeResponseDto {
    val requestUserId = userId.awaitSingle()
    return resumeFacadeService.updateResume(requestUserId, requestDto)
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun deleteResume(@AuthenticationPrincipal userId: Mono<String>, @RequestParam(required = true) id: String) {
    val requestUserId = userId.awaitSingle()
    resumeFacadeService.deleteResume(DeleteResumeRequestDto(id, requestUserId))
  }

}
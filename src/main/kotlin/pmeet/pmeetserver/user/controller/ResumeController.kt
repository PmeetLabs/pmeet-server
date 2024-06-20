package pmeet.pmeetserver.user.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService

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

}
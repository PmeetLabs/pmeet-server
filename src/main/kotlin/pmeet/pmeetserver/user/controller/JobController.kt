package pmeet.pmeetserver.user.controller

import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.job.request.CreateJobRequestDto
import pmeet.pmeetserver.user.dto.job.response.JobResponseDto
import pmeet.pmeetserver.user.service.job.JobFacadeService

@RestController
@RequestMapping("/api/v1/jobs")
class JobController(
  private val jobFacadeService: JobFacadeService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createJob(
    @RequestBody @Valid requestDto: CreateJobRequestDto
  ): JobResponseDto {
    return jobFacadeService.createJob(requestDto)
  }

  @GetMapping("/search")
  @ResponseStatus(HttpStatus.OK)
  suspend fun searchJobByName(
    @RequestParam(required = false) name: String?,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
  ): Slice<JobResponseDto> {
    return jobFacadeService.searchJobByName(name, PageRequest.of(page, size))
  }
}

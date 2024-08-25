package pmeet.pmeetserver.user.controller

import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.dto.resume.request.ChangeResumeActiveRequestDto
import pmeet.pmeetserver.user.dto.resume.request.CopyResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.UpdateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.resume.response.SearchedResumeResponseDto
import pmeet.pmeetserver.user.service.resume.ResumeFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/resumes")
class ResumeController(
  private val resumeFacadeService: ResumeFacadeService,
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createResume(
    @Valid @RequestBody requestDto: CreateResumeRequestDto
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

  @GetMapping("/list")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getResumeListByUserId(
    @AuthenticationPrincipal userId: Mono<String>
  ): List<ResumeResponseDto> {
    val requestUserId = userId.awaitSingle()
    return resumeFacadeService.findResumeListByUserId(requestUserId)
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  suspend fun updateResume(
    @AuthenticationPrincipal userId: Mono<String>,
    @Valid @RequestBody requestDto: UpdateResumeRequestDto
  ): ResumeResponseDto {
    val requestUserId = userId.awaitSingle()
    return resumeFacadeService.updateResume(requestUserId, requestDto)
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun deleteResume(@AuthenticationPrincipal userId: Mono<String>, @RequestParam(required = true) id: String) {
    val requestUserId = userId.awaitSingle()
    resumeFacadeService.deleteResume(DeleteResumeRequestDto(id, requestUserId))
  }

  @PostMapping("/copy")
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun copyResume(
    @AuthenticationPrincipal userId: Mono<String>,
    @Valid @RequestBody requestDto: CopyResumeRequestDto
  ): ResumeResponseDto {
    val requestUserId = userId.awaitSingle()
    return resumeFacadeService.copyResume(requestUserId, requestDto)
  }

  @PatchMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  suspend fun changeResumeActive(
    @AuthenticationPrincipal userId: Mono<String>,
    @Valid @RequestBody requestDto: ChangeResumeActiveRequestDto
  ) {
    val requestUserId = userId.awaitSingle()
    resumeFacadeService.changeResumeActiveStatus(requestUserId, requestDto)
  }

  @PutMapping("/{resumeId}/bookmark")
  @ResponseStatus(HttpStatus.OK)
  suspend fun addBookmarkResume(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable resumeId: String
  ) {
    resumeFacadeService.addBookmark(userId.awaitSingle(), resumeId)
  }

  @DeleteMapping("/{resumeId}/bookmark")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun removeBookmark(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable resumeId: String
  ) {
    resumeFacadeService.deleteBookmark(userId.awaitSingle(), resumeId)
  }

  @GetMapping("/bookmark-list")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getBookmarkedResumeList(
    @AuthenticationPrincipal userId: Mono<String>
  ): List<SearchedResumeResponseDto> {
    return resumeFacadeService.getBookmarkedResumeList(userId.awaitSingle())
  }

  @GetMapping("/search-slice")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getResumeListByCondition(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestParam(required = true) filterType: ResumeFilterType,
    @RequestParam(required = true) filterValue: String,
    @RequestParam(required = true) orderType: ResumeOrderType,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "8") size: Int,
  ): Slice<SearchedResumeResponseDto> {
    return resumeFacadeService.searchResumeSlice(
      userId.awaitSingle(),
      filterType,
      filterValue,
      orderType,
      PageRequest.of(page, size)
    )
  }
}

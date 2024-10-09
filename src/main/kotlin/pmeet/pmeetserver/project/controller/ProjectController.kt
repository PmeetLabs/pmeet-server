package pmeet.pmeetserver.project.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort.Direction
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.project.dto.comment.response.GetProjectCommentWithChildResponseDto
import pmeet.pmeetserver.project.dto.request.CompleteProjectRequestDto
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.request.SearchProjectRequestDto
import pmeet.pmeetserver.project.dto.request.UpdateProjectRequestDto
import pmeet.pmeetserver.project.dto.response.CompletedProjectResponseDto
import pmeet.pmeetserver.project.dto.response.GetMyProjectResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto
import pmeet.pmeetserver.project.dto.response.ProjectWithUserResponseDto
import pmeet.pmeetserver.project.dto.response.SearchCompleteProjectResponseDto
import pmeet.pmeetserver.project.dto.response.SearchProjectResponseDto
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.enums.ProjectSortProperty
import pmeet.pmeetserver.project.service.ProjectFacadeService
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/projects")
class ProjectController(
  private val projectFacadeService: ProjectFacadeService
) {

  @GetMapping("/{projectId}")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ): ProjectWithUserResponseDto {
    return projectFacadeService.getProjectByProjectId(userId.awaitSingle(), projectId)
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: CreateProjectRequestDto
  ): ProjectResponseDto {
    return projectFacadeService.createProject(userId.awaitSingle(), requestDto)
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  suspend fun updateProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestBody @Valid requestDto: UpdateProjectRequestDto
  ): ProjectResponseDto {
    return projectFacadeService.updateProject(userId.awaitSingle(), requestDto)
  }

  @DeleteMapping("/{projectId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun deleteProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ) {
    projectFacadeService.deleteProject(userId.awaitSingle(), projectId)
  }

  @GetMapping("/{projectId}/comments")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getProjectCommentList(
    @PathVariable projectId: String
  ): List<GetProjectCommentWithChildResponseDto> {
    return projectFacadeService.getProjectCommentList(projectId)
  }

  @GetMapping("/search-slice")
  @ResponseStatus(HttpStatus.OK)
  suspend fun searchProjectSlice(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestParam(defaultValue = "false") isCompleted: Boolean,
    @RequestParam(required = false) filterType: ProjectFilterType?,
    @RequestParam(required = false) filterValue: String?,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "8") size: Int,
    @RequestParam(defaultValue = "BOOK_MARKERS") sortBy: ProjectSortProperty,
    @RequestParam(defaultValue = "DESC") direction: Direction
  ): Slice<SearchProjectResponseDto> {
    val requestDto = SearchProjectRequestDto.of(isCompleted, filterType, filterValue, page, size, sortBy, direction)
    return projectFacadeService.searchProjectSlice(userId.awaitSingle(), requestDto)
  }

  @Operation(
    summary = "완표 프밋 목록을 slice 조회한다",
    description = "search-slice 와 유사하게 프밋 목록을 조회할 수 있다. 완료된 프밋만 검색되며, 해당 프밋의 팀원 목록을 같이 내려준다. isMy 프로퍼티를 통해서 마이 페이지에서도 조회에 사용할 수 있다."
  )
  @GetMapping("/complete/search-slice")
  @ResponseStatus(HttpStatus.OK)
  suspend fun searchCompleteProjectSlice(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestParam(required = false) filterType: ProjectFilterType?,
    @RequestParam(required = false) filterValue: String?,
    @RequestParam(required = false) isMy: Boolean?,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "8") size: Int,
    @RequestParam(defaultValue = "BOOK_MARKERS") sortBy: ProjectSortProperty,
    @RequestParam(defaultValue = "DESC") direction: Direction
  ): Slice<SearchCompleteProjectResponseDto> {
    val requestDto = SearchProjectRequestDto.of(true, filterType, filterValue, page, size, sortBy, direction)
    return projectFacadeService.searchCompleteProjectSlice(userId.awaitSingle(), requestDto, isMy)
  }


  @PutMapping("/{projectId}/bookmark")
  @ResponseStatus(HttpStatus.OK)
  suspend fun addBookmarkProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ) {
    projectFacadeService.addBookmark(userId.awaitSingle(), projectId)
  }

  @DeleteMapping("/{projectId}/bookmark")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun deleteBookmarkProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ) {
    projectFacadeService.deleteBookmark(userId.awaitSingle(), projectId)
  }

  @GetMapping("/my-project-slice")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getMyProjectSlice(
    @AuthenticationPrincipal userId: Mono<String>,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "6") size: Int
  ): Slice<GetMyProjectResponseDto> {
    return projectFacadeService.getMyProjectSlice(userId.awaitSingle(), PageRequest.of(page, size))
  }

  @GetMapping("/{projectId}/complete")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "완료 프밋 상세 조회", description = "완료된 프밋 작성을 위한 프밋 상세 조회")
  suspend fun getCompleteProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String
  ): CompletedProjectResponseDto {
    return projectFacadeService.getCompleteProject(userId.awaitSingle(), projectId)
  }

  @PutMapping("/{projectId}/complete")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "완료 프밋 생성 요청", description = "완료된 프밋에 대한 생성 요청")
  suspend fun updateCompleteProject(
    @AuthenticationPrincipal userId: Mono<String>,
    @PathVariable projectId: String,
    @RequestBody @Valid requestDto: CompleteProjectRequestDto
  ): CompletedProjectResponseDto {
    return projectFacadeService.updateCompleteProject(userId.awaitSingle(), projectId, requestDto)
  }
}

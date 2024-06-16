package pmeet.pmeetserver.user.controller

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
import pmeet.pmeetserver.user.dto.techStack.request.CreateTechStackRequestDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto
import pmeet.pmeetserver.user.service.techStack.TechStackFacadeService

@RestController
@RequestMapping("/api/v1/tech-stacks")
class TechStackController(
  private val techStackFacadeService: TechStackFacadeService
) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createTechStack(
    @RequestBody requestDto: CreateTechStackRequestDto
  ): TechStackResponseDto {
    return techStackFacadeService.createTechStack(requestDto)
  }

  @GetMapping("/search")
  @ResponseStatus(HttpStatus.OK)
  suspend fun searchTechStackByName(
    @RequestParam(required = false) name: String?,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "10") size: Int
  ): Slice<TechStackResponseDto> {
    return techStackFacadeService.searchTechStackByName(name, PageRequest.of(page, size))
  }
}

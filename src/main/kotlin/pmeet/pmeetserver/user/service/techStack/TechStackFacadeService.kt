package pmeet.pmeetserver.user.service.techStack

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.dto.techStack.request.CreateTechStackRequestDto
import pmeet.pmeetserver.user.dto.techStack.response.TechStackResponseDto

@Service
class TechStackFacadeService(
  private val techStackService: TechStackService
) {

  @Transactional
  suspend fun createTechStack(requestDto: CreateTechStackRequestDto): TechStackResponseDto {
    val techStack = TechStack(
      name = requestDto.name
    )
    return TechStackResponseDto.from(techStackService.save(techStack))
  }

  @Transactional(readOnly = true)
  suspend fun searchTechStackByName(name: String?, pageable: Pageable): Slice<TechStackResponseDto> {
    return techStackService.searchByTechStackName(name, pageable).map { TechStackResponseDto.from(it) }
  }

}
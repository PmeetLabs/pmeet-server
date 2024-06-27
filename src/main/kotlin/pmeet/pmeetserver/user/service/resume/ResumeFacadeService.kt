package pmeet.pmeetserver.user.service.resume

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.UpdateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto

@Service
class ResumeFacadeService(
  private val resumeService: ResumeService
) {

  @Transactional
  suspend fun createResume(requestDto: CreateResumeRequestDto): ResumeResponseDto {
    val resume = requestDto.toEntity()
    return ResumeResponseDto.from(resumeService.save(resume))
  }

  @Transactional(readOnly = true)
  suspend fun findResumeById(resumeId: String): ResumeResponseDto {
    return ResumeResponseDto.from(resumeService.findByResumeId(resumeId))
  }

  @Transactional
  suspend fun updateResume(requestDto: UpdateResumeRequestDto): ResumeResponseDto {
    val resume = requestDto.toEntity()
    return ResumeResponseDto.from(resumeService.update(resume, requestDto.id))
  }
}
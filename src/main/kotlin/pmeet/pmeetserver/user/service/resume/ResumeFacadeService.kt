package pmeet.pmeetserver.user.service.resume

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.UnauthorizedException
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
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
    return ResumeResponseDto.from(resumeService.getByResumeId(resumeId))
  }

  @Transactional
  suspend fun updateResume(userId: String, requestDto: UpdateResumeRequestDto): ResumeResponseDto {
    val originalResume = resumeService.getByResumeId(requestDto.id);
    if (!originalResume.userId.equals(userId)) {
      throw UnauthorizedException(ErrorCode.RESUME_UPDATE_UNAUTHORIZED)
    }
    val resume = requestDto.toEntity()
    return ResumeResponseDto.from(resumeService.update(originalResume.update(resume)))
  }

  @Transactional
  suspend fun deleteResume(requestDto: DeleteResumeRequestDto) {
    val originalResume = resumeService.getByResumeId(requestDto.id);
    if (!originalResume.userId.equals(requestDto.userId)) {
      throw UnauthorizedException(ErrorCode.RESUME_DELETE_UNAUTHORIZED)
    }
    resumeService.delete(requestDto.id, requestDto.userId)
  }
}
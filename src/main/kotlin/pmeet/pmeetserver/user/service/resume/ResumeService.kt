package pmeet.pmeetserver.user.service.resume

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.BadRequestException
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.repository.resume.ResumeRepository

@Service
class ResumeService(private val resumeRepository: ResumeRepository) {

  @Transactional
  suspend fun save(resume: Resume): Resume {
    val resumeNumber = resumeRepository.countByUserId(resume.userId).awaitSingle()
    if (resumeNumber < 5) {
      return resumeRepository.save(resume).awaitSingle()
    }
    throw BadRequestException(ErrorCode.RESUME_NUMBER_EXCEEDED)
  }

  @Transactional(readOnly = true)
  suspend fun findByResumeId(resumeId: String): Resume {
    return resumeRepository.findById(resumeId).awaitSingle()
  }
}
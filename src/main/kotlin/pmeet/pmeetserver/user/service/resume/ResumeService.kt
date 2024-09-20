package pmeet.pmeetserver.user.service.resume

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.BadRequestException
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.utils.page.SliceResponse
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.resume.Resume
import pmeet.pmeetserver.user.repository.resume.ResumeRepository
import pmeet.pmeetserver.user.repository.resume.vo.ProjectMemberWithResume

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
  suspend fun getByResumeId(resumeId: String): Resume {
    return resumeRepository.findById(resumeId).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.RESUME_NOT_FOUND)
  }

  @Transactional(readOnly = true)
  suspend fun getAllByUserId(userId: String): List<Resume> {
    return resumeRepository.findAllByUserId(userId).collectList().awaitSingle()
  }

  @Transactional
  suspend fun update(updateResume: Resume): Resume {
    return resumeRepository.save(updateResume).awaitSingle()
  }

  @Transactional
  suspend fun delete(deleteResume: Resume) {
    deleteResume.id?.let { resumeRepository.deleteById(it).awaitSingleOrNull() }
  }

  @Transactional
  suspend fun changeActive(originalResume: Resume, targetStatus: Boolean) {
    originalResume.isActive = targetStatus;
    resumeRepository.save(originalResume).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun getResumeListByResumeId(resumeIdList: List<String>): List<Resume> {
    return resumeRepository.findAllByIdIn(resumeIdList).collectList().awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun searchSliceByFilter(
    searchedUserId: String,
    filterType: ResumeFilterType,
    filterValue: String,
    orderType: ResumeOrderType,
    pageable: PageRequest
  ): Slice<Resume> {
    return SliceResponse.of(
      resumeRepository.findAllByFilter(searchedUserId, filterType, filterValue, orderType, pageable).collectList()
        .awaitSingle(),
      pageable
    )
  }

  @Transactional(readOnly = true)
  suspend fun getAllByProjectId(projectId: String): List<ProjectMemberWithResume> {
    return resumeRepository.findProjectMembersWithResumeByProjectId(projectId).collectList().awaitSingle()
  }
}

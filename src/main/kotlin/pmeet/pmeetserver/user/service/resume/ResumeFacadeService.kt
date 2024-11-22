package pmeet.pmeetserver.user.service.resume

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.ForbiddenRequestException
import pmeet.pmeetserver.file.service.FileService
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.dto.resume.request.ChangeResumeActiveRequestDto
import pmeet.pmeetserver.user.dto.resume.request.CopyResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.CreateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.DeleteResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.request.UpdateResumeRequestDto
import pmeet.pmeetserver.user.dto.resume.response.ResumeResponseDto
import pmeet.pmeetserver.user.dto.resume.response.SearchedResumeResponseDto
import pmeet.pmeetserver.user.service.UserService

@Service
class ResumeFacadeService(
  private val resumeService: ResumeService,
  private val fileService: FileService,
  private val userService: UserService
) {

  @Transactional
  suspend fun createResume(requestDto: CreateResumeRequestDto): ResumeResponseDto {
    val resume = requestDto.toEntity()
    return ResumeResponseDto.of(
      resumeService.save(resume),
      resume.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      resume.portfolioFileUrls?.let { fileService.generatePreSignedUrlsToDownload(it) }
    )
  }

  @Transactional(readOnly = true)
  suspend fun findResumeById(resumeId: String): ResumeResponseDto {
    val resume = resumeService.getByResumeId(resumeId)
    return ResumeResponseDto.of(
      resume,
      resume.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      resume.portfolioFileUrls?.let { fileService.generatePreSignedUrlsToDownload(it) }
    )
  }

  @Transactional(readOnly = true)
  suspend fun findResumeListByUserId(userId: String): List<ResumeResponseDto> {
    val resumes = resumeService.getAllByUserId(userId)
    return resumes.map {
      ResumeResponseDto.of(
        it,
        it.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
        it.portfolioFileUrls?.let { fileService.generatePreSignedUrlsToDownload(it) }
      )
    }
  }

  @Transactional
  suspend fun updateResume(userId: String, requestDto: UpdateResumeRequestDto): ResumeResponseDto {
    val originalResume = resumeService.getByResumeId(requestDto.id);
    if (!originalResume.userId.equals(userId)) {
      throw ForbiddenRequestException(ErrorCode.RESUME_UPDATE_FORBIDDEN)
    }
    val updateResume = originalResume.update(
      title = requestDto.title,
      userProfileImageUrl = requestDto.userProfileImageUrl,
      desiredJobs = requestDto.desiredJobs.map { it.toEntity() },
      techStacks = requestDto.techStacks.map { it.toEntity() },
      jobExperiences = requestDto.jobExperiences.map { it.toEntity() },
      projectExperiences = requestDto.projectExperiences.map { it.toEntity() },
      portfolioFileUrls = requestDto.portfolioFileUrls,
      portfolioUrl = requestDto.portfolioUrl,
      selfDescription = requestDto.selfDescription
    )
    val updatedResume = resumeService.update(updateResume)
    return ResumeResponseDto.of(
      updatedResume,
      updatedResume.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      updatedResume.portfolioFileUrls?.let { fileService.generatePreSignedUrlsToDownload(it) }
    )
  }

  @Transactional
  suspend fun deleteResume(requestDto: DeleteResumeRequestDto) {
    val originalResume = resumeService.getByResumeId(requestDto.id);
    if (!originalResume.userId.equals(requestDto.userId)) {
      throw ForbiddenRequestException(ErrorCode.RESUME_DELETE_FORBIDDEN)
    }
    resumeService.delete(originalResume)
  }

  @Transactional
  suspend fun copyResume(userId: String, requestDto: CopyResumeRequestDto): ResumeResponseDto {
    val originalResume = resumeService.getByResumeId(requestDto.id);
    if (!originalResume.userId.equals(userId)) {
      throw ForbiddenRequestException(ErrorCode.RESUME_COPY_FORBIDDEN)
    }
    val copiedResume = resumeService.save(originalResume.copy())
    return ResumeResponseDto.of(
      copiedResume,
      copiedResume.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      copiedResume.portfolioFileUrls?.let { fileService.generatePreSignedUrlsToDownload(it) }
    )
  }

  @Transactional
  suspend fun changeResumeActiveStatus(userId: String, requestDto: ChangeResumeActiveRequestDto) {
    val originalResume = resumeService.getByResumeId(requestDto.id)
    if (!originalResume.userId.equals(userId)) {
      throw ForbiddenRequestException(ErrorCode.RESUME_ACTIVE_CHANGE_FORBIDDEN)
    }
    resumeService.changeActive(originalResume, requestDto.targetActiveStatus)
  }

  @Transactional
  suspend fun addBookmark(userId: String, resumeId: String) {
    val user = userService.getUserById(userId)
    user.addBookmarkForResume(resumeId)
    userService.update(user)
    val resume = resumeService.getByResumeId(resumeId)
    resume.addBookmark(userId)
    resumeService.update(resume)
  }

  @Transactional
  suspend fun deleteBookmark(userId: String, resumeId: String) {
    val user = userService.getUserById(userId)
    user.deleteBookmarkForResume(resumeId)
    userService.update(user)
    val resume = resumeService.getByResumeId(resumeId)
    resume.deleteBookmark(userId)
    resumeService.update(resume)
  }

  @Transactional
  suspend fun getBookmarkedResumeList(userId: String): List<SearchedResumeResponseDto> {
    val user = userService.getUserById(userId)
    val resumeList = resumeService.getResumeListByResumeId(user.bookmarkedResumes.map { it.resumeId })
    return resumeList.filter { it.isActive }.map {
      SearchedResumeResponseDto.of(
        it,
        it.userProfileImageUrl?.let { it1 -> fileService.generatePreSignedUrlToDownload(it1) },
        userId
      )
    }.toList()
  }

  @Transactional
  suspend fun searchResumeSlice(
    userId: String,
    filterType: ResumeFilterType?,
    filterValue: String?,
    orderType: ResumeOrderType,
    pageable: PageRequest
  ): Slice<SearchedResumeResponseDto> {
    val resumes = resumeService.searchSliceByFilter(userId, filterType, filterValue, orderType, pageable)
    return SliceImpl(
      resumes.content.map {
        SearchedResumeResponseDto.of(
          it,
          it.userProfileImageUrl?.let { it1 -> fileService.generatePreSignedUrlToDownload(it1) },
          userId
        )
      },
      resumes.pageable,
      resumes.hasNext()
    )
  }

  @Transactional(readOnly = true)
  suspend fun findResumeListByProjectId(userId: String, projectId: String): List<ResumeResponseDto> {
    val resumeListInProject = resumeService.getAllByProjectId(projectId)
    return resumeListInProject.map {
      ResumeResponseDto.of(
        resume = it.resume,
        profileImageDownloadUrl = it.resume.userProfileImageUrl?.let { fileService.generatePreSignedUrlToDownload(it) },
      )
    }
  }
}

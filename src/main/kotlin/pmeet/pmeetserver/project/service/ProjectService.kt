package pmeet.pmeetserver.project.service

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.common.utils.page.SliceResponse
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.dto.request.CompleteProjectRequestDto
import pmeet.pmeetserver.project.enums.ProjectFilterType
import pmeet.pmeetserver.project.repository.ProjectRepository

@Service
class ProjectService(
  private val projectRepository: ProjectRepository
) {

  @Transactional
  suspend fun save(project: Project): Project {
    return projectRepository.save(project).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun getProjectById(projectId: String): Project {
    return projectRepository.findById(projectId).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND)
  }

  @Transactional
  suspend fun update(project: Project): Project {
    return projectRepository.save(project).awaitSingle()
  }

  @Transactional
  suspend fun delete(project: Project) {
    projectRepository.delete(project).awaitSingleOrNull()
  }

  @Transactional(readOnly = true)
  suspend fun searchSliceByFilter(
    isCompleted: Boolean,
    filterType: ProjectFilterType?,
    filterValue: String?,
    userId: String,
    isMy: Boolean,
    pageable: Pageable
  ): Slice<Project> {
    return SliceResponse.of(
      projectRepository.findAllByFilter(
        isCompleted,
        filterType,
        filterValue,
        userId,
        isMy,
        pageable
      ).collectList().awaitSingle(),
      pageable
    )
  }

  @Transactional(readOnly = true)
  suspend fun getProjectSliceByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Slice<Project> {
    return SliceResponse.of(
      projectRepository.findProjectByUserIdOrderByCreatedAtDesc(
        userId,
        pageable
      ).collectList().awaitSingle(),
      pageable
    )
  }

  @Transactional
  suspend fun completeProject(project: Project, requestDto: CompleteProjectRequestDto): Project {
    project.update(
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      description = requestDto.description,
      completeAttachments = requestDto.attachmentUrls
    )
    project.complete()
    return projectRepository.save(project).awaitSingle()
  }
}

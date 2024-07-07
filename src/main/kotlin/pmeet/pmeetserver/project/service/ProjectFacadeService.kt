package pmeet.pmeetserver.project.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.domain.Recruitment
import pmeet.pmeetserver.project.dto.request.CreateProjectRequestDto
import pmeet.pmeetserver.project.dto.response.ProjectResponseDto

@Service
class ProjectFacadeService(
  private val projectService: ProjectService
) {

  @Transactional
  suspend fun createProject(userId: String, requestDto: CreateProjectRequestDto): ProjectResponseDto {
    val recruitments =
      requestDto
        .recruitments
        .map { Recruitment(it.jobName, it.numberOfRecruitment) }
        .toList()
    val project = Project(
      userId = userId,
      title = requestDto.title,
      startDate = requestDto.startDate,
      endDate = requestDto.endDate,
      thumbNailUrl = requestDto.thumbNailUrl,
      techStacks = requestDto.techStacks,
      recruitments = recruitments,
      description = requestDto.description
    )

    return ProjectResponseDto.from(projectService.save(project))
  }
}

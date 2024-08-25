package pmeet.pmeetserver.project.repository

import org.springframework.data.domain.Pageable
import pmeet.pmeetserver.project.domain.Project
import pmeet.pmeetserver.project.enums.ProjectFilterType
import reactor.core.publisher.Flux

interface CustomProjectRepository {

  /**
   * 프로젝트 필터링을 통한 프로젝트 목록 조회
   *
   * @param isCompleted 완료 여부
   * @param filterType ? 필터 타입(ALL, TITLE, JOB_NAME)
   * @param filterValue ? 필터 값
   * @param pageable 페이징 정보
   */
  fun findAllByFilter(
    isCompleted: Boolean,
    filterType: ProjectFilterType?,
    filterValue: String?,
    pageable: Pageable
  ): Flux<Project>


  /**
   * User ID로 프로젝트 목록 조회
   *
   * @param userId 사용자 ID
   * @param pageable 페이징 정보
   */
  fun findProjectByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Flux<Project>
}

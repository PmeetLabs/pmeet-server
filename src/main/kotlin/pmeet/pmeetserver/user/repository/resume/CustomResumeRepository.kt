package pmeet.pmeetserver.user.repository.resume

import org.springframework.data.domain.Pageable
import pmeet.pmeetserver.user.domain.enum.ResumeFilterType
import pmeet.pmeetserver.user.domain.enum.ResumeOrderType
import pmeet.pmeetserver.user.domain.resume.Resume
import reactor.core.publisher.Flux

interface CustomResumeRepository {

  /**
   * 이력서 필터링을 통한 이력서 목록 조회
   *
   * @param filterType 검색 종류
   * @param filterValue 검색어
   * @param orderType 정렬 순서
   * @param pageable 페이징 정보
   *
   */
  fun findAllByFilter(
    searchedUserId: String,
    filterType: ResumeFilterType,
    filterValue: String,
    orderType: ResumeOrderType,
    pageable: Pageable
  ): Flux<Resume>

}

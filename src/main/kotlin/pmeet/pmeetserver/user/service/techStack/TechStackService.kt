package pmeet.pmeetserver.user.service.techStack

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.common.utils.page.SliceResponse
import pmeet.pmeetserver.user.domain.techStack.TechStack
import pmeet.pmeetserver.user.repository.techStack.TechStackRepository
import org.springframework.data.domain.Slice

@Service
class TechStackService(private val techStackRepository: TechStackRepository) {

  @Transactional
  suspend fun save(techStack: TechStack): TechStack {
    techStackRepository.findByName(techStack.name).awaitSingleOrNull()?.let { throw EntityDuplicateException(ErrorCode.TECHSTACK_DUPLICATE_BY_NAME) }
    return techStackRepository.save(techStack).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun searchByTechStackName(name: String?, pageable: Pageable): Slice<TechStack> {
    return SliceResponse.of(techStackRepository.findByNameSearchSlice(name, pageable).collectList().awaitSingle(), pageable)
  }

}
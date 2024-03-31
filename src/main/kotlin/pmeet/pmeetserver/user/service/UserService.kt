package pmeet.pmeetserver.user.service

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.common.exception.EntityNotFoundException
import pmeet.pmeetserver.user.domain.User
import pmeet.pmeetserver.user.repository.UserRepository

@Service
class UserService(
  private val userRepository: UserRepository
) {
  @Transactional
  suspend fun save(user: User): User {
    userRepository.findByEmail(user.email).awaitSingleOrNull()?.let {
      throw EntityDuplicateException(ErrorCode.USER_DUPLICATE_BY_EMAIL)
    }

    userRepository.findByNickname(user.nickname).awaitSingleOrNull()?.let {
      throw EntityDuplicateException(ErrorCode.USER_DUPLICATE_BY_NICKNAME)
    }

    return userRepository.save(user).awaitSingle()
  }

  @Transactional(readOnly = true)
  suspend fun getUserByNickname(nickname: String): User {
    return userRepository.findByNickname(nickname).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND_BY_NICKNAME)
  }

  @Transactional(readOnly = true)
  suspend fun getUserByEmail(email: String): User {
    return userRepository.findByEmail(email).awaitSingleOrNull()
      ?: throw EntityNotFoundException(ErrorCode.USER_NOT_FOUND_BY_EMAIL)
  }
}

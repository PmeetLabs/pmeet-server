package pmeet.pmeetserver.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.common.ErrorCode
import pmeet.pmeetserver.common.exception.EntityDuplicateException
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto

@Service
class UserFacadeService(
  private val userService: UserService
) {

  @Transactional(readOnly = true)
  suspend fun isDuplicateNickName(requestDto: CheckNickNameRequestDto): Boolean {
    userService.getUserByNickname(requestDto.nickname)?.let {
      throw EntityDuplicateException(ErrorCode.USER_DUPLICATE_BY_NICKNAME)
    }
    return false
  }

}


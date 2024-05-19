package pmeet.pmeetserver.user.controller

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.response.UserResponseDto
import pmeet.pmeetserver.user.dto.response.UserSummaryResponseDto
import pmeet.pmeetserver.user.service.UserFacadeService
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/v1/users")
class UserController(
  private val userFacadeService: UserFacadeService
) {

  @GetMapping("/me/summary")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getMySummaryInfo(@AuthenticationPrincipal userId: Mono<String>): UserSummaryResponseDto {
    return userFacadeService.getMySummaryInfo(userId.awaitSingle())
  }

  @GetMapping("/me")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getMyInfo(@AuthenticationPrincipal userId: Mono<String>): UserResponseDto {
    return userFacadeService.getMyInfo(userId.awaitSingle())
  }
}

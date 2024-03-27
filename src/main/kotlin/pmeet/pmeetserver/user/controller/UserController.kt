package pmeet.pmeetserver.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.request.CheckNickNameRequestDto
import pmeet.pmeetserver.user.service.UserFacadeService
import pmeet.pmeetserver.user.service.UserService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
  private val userService: UserService,
  private val userFacadeService: UserFacadeService
) {
  @PostMapping("/nickname/duplicate")
  @ResponseStatus(HttpStatus.OK)
  suspend fun getNickname(@RequestBody @Valid requestDto: CheckNickNameRequestDto): Boolean {
    return userFacadeService.isDuplicateNickName(requestDto)
  }
}

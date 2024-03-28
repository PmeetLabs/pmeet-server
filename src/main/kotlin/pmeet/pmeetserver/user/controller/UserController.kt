package pmeet.pmeetserver.user.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.service.UserFacadeService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
  private val userFacadeService: UserFacadeService
) {

}

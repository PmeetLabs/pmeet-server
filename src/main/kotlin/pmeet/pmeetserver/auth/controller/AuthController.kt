package pmeet.pmeetserver.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.user.dto.SignUpRequestDto
import pmeet.pmeetserver.user.dto.UserResponseDto
import pmeet.pmeetserver.user.service.UserService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
  private val userService: UserService
) {
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createUser(@RequestBody @Valid requestDto: SignUpRequestDto): UserResponseDto {
    return userService.save(requestDto)
  }
}


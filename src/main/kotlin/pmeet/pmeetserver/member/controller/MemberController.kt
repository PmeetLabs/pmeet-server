package pmeet.pmeetserver.member.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pmeet.pmeetserver.member.domain.Member
import pmeet.pmeetserver.member.dto.SignUpRequestDto
import pmeet.pmeetserver.member.service.MemberService

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
  private val memberService: MemberService
) {

  // TODO will be moved to AuthController
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  suspend fun createMember(@RequestBody requestDto: SignUpRequestDto): Member {
    return memberService.save(requestDto)
  }
}


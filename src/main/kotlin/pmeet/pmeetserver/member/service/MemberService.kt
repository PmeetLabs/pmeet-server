package pmeet.pmeetserver.member.service

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pmeet.pmeetserver.member.domain.Member
import pmeet.pmeetserver.member.dto.SignUpRequestDto
import pmeet.pmeetserver.member.repository.MemberRepository

@Service
class MemberService(
  private val memberRepository: MemberRepository
) {
  @Transactional
  suspend fun save(requestDto: SignUpRequestDto): Member {
    return memberRepository.save(
      Member(
        email = requestDto.email,
        password = requestDto.password,
        nickname = requestDto.nickname
      )
    ).awaitSingle()
  }
}

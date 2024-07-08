package pmeet.pmeetserver.user.dto.resume.request
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateResumeRequestDto(
  val id: String,
  @field:NotBlank(message = "제목은 필수입니다.")
  @field:Size(min = 1, max = 30, message = "제목은 1글자에서 30글자 사이여야 합니다.")
  @field:Pattern(
    regexp = "^[가-힣a-zA-Z0-9\\p{Punct}\\s]*$",
    message = "제목은 한글, 영어, 숫자, 문장부호만 입력 가능합니다."
  )
  val title: String,
  val isActive: Boolean,
  val userProfileImageUrl: String?,
  @field:Size(max = 5, message = "최대 5개의 희망 직무만 입력 가능합니다.")
  val desiredJobs: List<ResumeJobRequestDto>,
  @field:Size(max = 5, message = "최대 5개의 기술 스택만 입력 가능합니다.")
  val techStacks: List<ResumeTechStackRequestDto>,
  val jobExperiences: List<ResumeJobExperienceRequestDto>,
  val projectExperiences: List<ResumeProjectExperienceRequestDto>,
  val portfolioFileUrl: String?,
  @field:Size(max = 3, message = "최대 3개의 포트폴리오 링크만 입력 가능합니다.")
  val portfolioUrl: List<String>,
  @field:Size(max = 500, message = "자기소개는 최대 500자까지 입력 가능합니다.")
  val selfDescription: String?
)
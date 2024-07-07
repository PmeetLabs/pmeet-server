package pmeet.pmeetserver.project.dto.request

import jakarta.validation.constraints.NotBlank

data class RecruitmentRequestDto(
  @field:NotBlank(message = "직무명은 필수입니다.")
  val jobName: String,
  var numberOfRecruitment: Int
) {
}


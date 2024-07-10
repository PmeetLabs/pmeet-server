package pmeet.pmeetserver.project.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class RecruitmentRequestDto(
  @field:NotBlank(message = "직무명은 필수입니다.")
  val jobName: String,
  @field:Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다.")
  var numberOfRecruitment: Int
) {
}


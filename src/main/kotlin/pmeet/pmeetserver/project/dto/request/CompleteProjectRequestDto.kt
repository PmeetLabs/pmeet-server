package pmeet.pmeetserver.project.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.UniqueElements
import pmeet.pmeetserver.project.validation.annotation.ValidDateRange
import java.time.LocalDateTime

@ValidDateRange
data class CompleteProjectRequestDto(
  @field:NotBlank(message = "프로젝트 ID는 필수입니다.")
  val id: String,
  @field:NotBlank(message = "프로젝트 제목은 필수입니다.")
  @field:Size(min = 1, max = 30, message = "프로젝트 제목은 1자에서 30자 사이여야 합니다.")
  @field:Pattern(
    regexp = "^[가-힣a-zA-Z0-9\\p{Punct}\\s]*$",
    message = "프로젝트 제목은 한글, 영어, 숫자, 문장부호만 입력 가능합니다."
  )
  val title: String,
  @field:NotNull(message = "프로젝트 시작일은 필수입니다.")
  val startDate: LocalDateTime,
  @field:NotNull(message = "프로젝트 종료일은 필수입니다.")
  val endDate: LocalDateTime,
  val thumbNailUrl: String? = null,
  @field:Size(max = 5, message = "기술 스택은 5개까지만 입력 가능합니다.")
  @field:UniqueElements(message = "기술 스택은 중복되지 않아야 합니다.")
  val techStacks: List<String>? = mutableListOf(),
  val projectMemberResumeId: List<String>,
  @field:NotBlank(message = "프로젝트 설명은 필수입니다.")
  @field:Size(min = 1, max = 2000, message = "프로젝트 설명은 1자에서 2000자 사이여야 합니다.")
  val description: String,
  @field:Size(min = 1, message = "완료된 링크에 대한 첨부는 필수입니다.")
  val attachmentUrls: List<String>
) {
}

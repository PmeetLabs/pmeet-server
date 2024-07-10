package pmeet.pmeetserver.project.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pmeet.pmeetserver.project.validation.annotation.ValidDateRange
import java.time.LocalDateTime

class DateRangeValidator : ConstraintValidator<ValidDateRange, Any> {

  companion object {
    private const val START_DATE_FIELD_NAME = "startDate"
    private const val END_DATE_FIELD_NAME = "endDate"
  }

  /**
   * 종료일이 시작일보다 이후인지 검증합니다.
   *
   * 종료일 또는 시작일이 null이면 유효한 것으로 판단합니다.
   *
   */
  override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return true

    val startDate: LocalDateTime? = getPropertyValue(value, START_DATE_FIELD_NAME)
    val endDate: LocalDateTime? = getPropertyValue(value, END_DATE_FIELD_NAME)

    if (startDate == null || endDate == null) return true

    if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
      /*
        * 이 어노테이션은 클래스 레벨에 붙어있으므로, property node를 지정하기 위해 context를 사용합니다.
       */
      context?.apply {
        disableDefaultConstraintViolation()
        buildConstraintViolationWithTemplate(defaultConstraintMessageTemplate)
          .addPropertyNode(END_DATE_FIELD_NAME)
          .addConstraintViolation()
      }
      return false
    }
    return true
  }

  private fun getPropertyValue(obj: Any, propertyName: String): LocalDateTime? {
    val property = obj::class.members.find { it.name == propertyName } ?: return null
    return property.call(obj) as? LocalDateTime
  }
}

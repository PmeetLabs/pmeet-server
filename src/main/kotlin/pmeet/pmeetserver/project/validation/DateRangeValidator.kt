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

  override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return true

    val startDate: LocalDateTime? = getPropertyValue(value, START_DATE_FIELD_NAME)
    val endDate: LocalDateTime? = getPropertyValue(value, END_DATE_FIELD_NAME)

    if (startDate == null || endDate == null) return true

    if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
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

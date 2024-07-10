package pmeet.pmeetserver.project.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pmeet.pmeetserver.project.validation.annotation.TotalSumMax
import kotlin.reflect.full.memberProperties

class TotalSumMaxValidator : ConstraintValidator<TotalSumMax, Collection<*>> {
  private var max: Int = 0
  private lateinit var element: String

  override fun initialize(constraintAnnotation: TotalSumMax) {
    this.max = constraintAnnotation.sum
    this.element = constraintAnnotation.element
  }

  override fun isValid(value: Collection<*>?, context: ConstraintValidatorContext): Boolean {
    if (value == null) return true

    val total = value.sumOf {
      it?.let { item ->
        item::class.memberProperties
          .find { prop -> prop.name == element }
          ?.getter
          ?.call(item) as? Int ?: 0
      } ?: 0
    }

    return total <= max
  }
}

package pmeet.pmeetserver.project.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pmeet.pmeetserver.project.validation.DateRangeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateRangeValidator::class])
annotation class ValidDateRange(
  val message: String = "종료일은 시작일보다 이후여야 합니다.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

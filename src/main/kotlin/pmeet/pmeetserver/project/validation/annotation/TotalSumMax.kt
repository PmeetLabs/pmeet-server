package pmeet.pmeetserver.project.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pmeet.pmeetserver.project.validation.TotalSumMaxValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TotalSumMaxValidator::class])
annotation class TotalSumMax(
  val message: String = "모집 인원의 합이 {value}을 초과할 수 없습니다.",
  val value: Int,
  val element: String = "",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

package pmeet.pmeetserver.project.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pmeet.pmeetserver.project.validation.TotalSumMaxValidator
import kotlin.reflect.KClass

/**
 * Collection 타입의 요소들의 특정 필드의 합이 주어진 값보다 작거나 같아야 하는지 검증하는 애노테이션
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TotalSumMaxValidator::class])
annotation class TotalSumMax(
  /**
   * 검증 실패 시 출력할 메시지
   */
  val message: String,
  /**
   * 합의 최대값
   */
  val sum: Int,
  /**
   * 검증할 요소의 필드명
   */
  val element: String = "",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

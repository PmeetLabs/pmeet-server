package pmeet.pmeetserver.project.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pmeet.pmeetserver.project.validation.DateRangeValidator
import kotlin.reflect.KClass

/**
 * 종료일이 시작일보다 이후인지 검증합니다.
 *
 * 종료일 또는 시작일이 null이면 유효한 것으로 판단합니다.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateRangeValidator::class])
annotation class ValidDateRange(
  /**
   * 검증 실패 시 출력할 메시지
   */
  val message: String = "종료일은 시작일보다 이후여야 합니다.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

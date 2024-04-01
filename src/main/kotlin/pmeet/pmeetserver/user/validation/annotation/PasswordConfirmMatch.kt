package pmeet.pmeetserver.user.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pmeet.pmeetserver.user.validation.PasswordConfirmMatchValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordConfirmMatchValidator::class])
annotation class PasswordConfirmMatch(
  val message: String = "비밀번호가 일치하지 않습니다.",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

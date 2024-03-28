package pmeet.pmeetserver.user.validation.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass
import pmeet.pmeetserver.user.validation.PasswordValidator

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class Password(
  val message: String,
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)

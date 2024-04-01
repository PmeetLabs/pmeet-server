package pmeet.pmeetserver.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pmeet.pmeetserver.user.validation.annotation.PasswordConfirmMatch

class PasswordConfirmMatchValidator : ConstraintValidator<PasswordConfirmMatch, Any> {

  companion object {
    private const val PASSWORD_FIELD_NAME = "password"
    private const val PASSWORD_CONFIRM_FIELD_NAME = "passwordConfirm"
  }

  override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) return true

    val password = value::class.members.firstOrNull { it.name == PASSWORD_FIELD_NAME }?.call(value) as String?
    val passwordConfirm =
      value::class.members.firstOrNull { it.name == PASSWORD_CONFIRM_FIELD_NAME }?.call(value) as String?

    if (password == null || passwordConfirm == null) return true

    if (password != passwordConfirm) {
      context?.apply {
        disableDefaultConstraintViolation()
        buildConstraintViolationWithTemplate(defaultConstraintMessageTemplate)
          .addPropertyNode(PASSWORD_CONFIRM_FIELD_NAME)
          .addConstraintViolation()
      }
      return false
    }

    return true
  }
}

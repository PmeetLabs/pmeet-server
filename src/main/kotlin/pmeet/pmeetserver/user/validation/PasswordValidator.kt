package pmeet.pmeetserver.user.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pmeet.pmeetserver.user.validation.annotation.Password

class PasswordValidator : ConstraintValidator<Password, String> {
  override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
    if (value == null) {
      return false
    }

    val lengthRegex = "^.{8,16}$".toRegex()
    val digitRegex = ".*[0-9].*".toRegex()
    val letterRegex = ".*[a-zA-Z].*".toRegex()
    val specialCharRegex = ".*[!\"#$%&'()*+,-./:;<=>?@₩^_{|}~].*".toRegex()

    val matchers = listOf(digitRegex, letterRegex, specialCharRegex)
      .count { it.matches(value) }

    return value.matches(lengthRegex) && matchers >= 2
  }
}

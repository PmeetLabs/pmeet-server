package pmeet.pmeetserver.common.generator

class VerificationCodeGenerator {

  companion object {
    private const val VERIFICATION_CODE_LENGTH = 6
    private val VERIFICATION_CODE_CHARACTERS = ('0'..'9') + ('A'..'Z') + ('a'..'z')

    fun generateVerificationCode(): String {
      return List(VERIFICATION_CODE_LENGTH) { VERIFICATION_CODE_CHARACTERS.random() }.joinToString("")
    }
  }

}

package pmeet.pmeetserver.common

enum class ErrorCode(private val code: String, private val message: String) {
  INVALID_INPUT_PARAMETER("COMMON-40000", "유효하지 않은 입력값입니다."),

  USER_DUPLICATE_BY_EMAIL("USER-40000", "중복된 이메일입니다."),
  USER_DUPLICATE_BY_NICKNAME("USER-40001", "중복된 닉네임입니다."),
  USER_NOT_FOUND_BY_EMAIL("USER-40002", "해당하는 이메일의 유저를 찾을 수 없습니다."),
  INVALID_PASSWORD("USER-40003", "비밀번호를 다시 입력해 주세요."),
  VERIFICATION_CODE_NOT_MATCH("USER-40004", "인증번호가 일치하지 않습니다."),
  VERIFICATION_CODE_EXPIRED("USER-40005", "인증번호가 만료되었습니다."),
  USER_NOT_FOUND_BY_NICKNAME("USER-40006", "해당하는 닉네임의 유저를 찾을 수 없습니다.")
  ;


  fun getCode(): String {
    return this.code
  }

  fun getMessage(): String {
    return this.message
  }
}

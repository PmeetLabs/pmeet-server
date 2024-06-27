package pmeet.pmeetserver.common

enum class ErrorCode(private val code: String, private val message: String) {
  INVALID_TOKEN("AUTH-40100", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN("AUTH-40101", "토큰의 유효기간이 만료되었습니다."),
  VERIFICATION_CODE_NOT_MATCH("AUTH-40102", "인증번호가 일치하지 않습니다."),
  VERIFICATION_CODE_EXPIRED("AUTH-40103", "인증번호가 만료되었습니다."),
  NOT_VERIFIED_EMAIL("AUTH-40104", "인증을 다시 시도해 주세요."),

  INVALID_INPUT_PARAMETER("COMMON-40000", "유효하지 않은 입력값입니다."),

  // User
  USER_DUPLICATE_BY_EMAIL("USER-40000", "중복된 이메일입니다."),
  USER_DUPLICATE_BY_NICKNAME("USER-40001", "중복된 닉네임입니다."),
  USER_NOT_FOUND_BY_EMAIL("USER-40002", "해당하는 이메일의 유저를 찾을 수 없습니다."),
  INVALID_PASSWORD("USER-40003", "비밀번호를 다시 입력해 주세요."),
  USER_NOT_FOUND_BY_NICKNAME("USER-40006", "해당하는 닉네임의 유저를 찾을 수 없습니다."),
  USER_NOT_FOUND_BY_ID("USER-40007", "해당하는 ID의 유저를 찾을 수 없습니다."),
  IS_DELETED_USER("USER-40008", "탈퇴한 유저입니다."),

  // Job
  JOB_DUPLICATE_BY_NAME("JOB-40000", "중복된 직무명입니다."),

  // TechStack
  TECHSTACK_DUPLICATE_BY_NAME("TECHSTACH-40000", "중복된 기술스택입니다."),

  // Resume
  RESUME_NUMBER_EXCEEDED("RESUME-40000", "이력서는 5개까지만 생성 가능합니다."),
  RESUME_NOT_FOUND("RESUME-40001", "해당하는 ID 의 이력서를 찾을 수 없습니다.")
  ;

  fun getCode(): String {
    return this.code
  }

  fun getMessage(): String {
    return this.message
  }
}

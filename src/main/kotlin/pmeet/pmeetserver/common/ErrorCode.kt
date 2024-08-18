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
  INVALID_PASSWORD("USER-40003", "비밀번호를 다시 입력해 주세요."),
  IS_DELETED_USER("USER-40004", "탈퇴한 유저입니다."),

  USER_NOT_FOUND_BY_EMAIL("USER-40400", "해당하는 이메일의 유저를 찾을 수 없습니다."),
  USER_NOT_FOUND_BY_NICKNAME("USER-40401", "해당하는 닉네임의 유저를 찾을 수 없습니다."),
  USER_NOT_FOUND_BY_ID("USER-40402", "해당하는 ID의 유저를 찾을 수 없습니다."),

  // Job
  JOB_DUPLICATE_BY_NAME("JOB-40000", "중복된 직무명입니다."),

  // TechStack
  TECHSTACK_DUPLICATE_BY_NAME("TECHSTACK-40000", "중복된 기술스택입니다."),

  // Resume
  RESUME_NUMBER_EXCEEDED("RESUME-40000", "이력서는 5개까지만 생성 가능합니다."),

  RESUME_UPDATE_FORBIDDEN("RESUME-40301", "해당하는 이력서를 수정할 권한이 없습니다."),
  RESUME_DELETE_FORBIDDEN("RESUME-40302", "해당하는 이력서를 삭제할 권한이 없습니다."),
  RESUME_COPY_FORBIDDEN("RESUME-40303", "해당하는 이력서를 복사할 권한이 없습니다."),
  RESUME_ACTIVE_CHANGE_FORBIDDEN("RESUME-40304", "해당하는 이력서의 프미팅 상태를 변경할 권한이 없습니다."),
  RESUME_TRYOUT_FORBIDDEN("RESUME-40305", "해당하는 이력서로 프로젝트에 지원할 권한이 없습니다."),
  RESUME_NOT_FOUND("RESUME-40400", "해당하는 ID의 이력서를 찾을 수 없습니다."),

  // Project
  PROJECT_NOT_FOUND("PROJECT-40400", "해당하는 ID의 프로젝트를 찾을 수 없습니다."),
  PROJECT_UPDATE_FORBIDDEN("PROJECT-40300", "해당하는 프로젝트를 수정할 권한이 없습니다."),
  PROJECT_DELETE_FORBIDDEN("PROJECT-40301", "해당하는 프로젝트를 삭제할 권한이 없습니다."),
  PROJECT_TRYOUT_VIEW_FORBIDDEN("PRJECT-40302", "해당하는 프로젝트의 지원자를 볼 권한이 없습니다."),

  // Project Comment
  PROJECT_COMMENT_NOT_FOUND("PROJECT-COMMENT-40400", "해당하는 ID의 댓글을 찾을 수 없습니다."),
  PROJECT_COMMENT_DELETE_FORBIDDEN("PROJECT-COMMENT-40300", "해당하는 댓글을 삭제할 권한이 없습니다."),

  // Project Tryout
  PROJECT_TRYOUT_STATUS_UPDATE_FAIL("PROJECT-TRYOUT-40401", "상태가 검토중이 아닌 지원서는 상태를 변경할 수 없습니다."),

  // Project Member
  PROJECT_MEMBER_MODIFY_FORBIDDEN("PROJECT-MEMBER-40401", "프로젝트 멤버를 수정할 권한이 없습니다."),

  ;

  fun getCode(): String {
    return this.code
  }

  fun getMessage(): String {
    return this.message
  }
}

package pmeet.pmeetserver.common

enum class ErrorCode(private val code: String, private val message: String) {
  INVALID_INPUT_PARAMETER("COMMON-40000", "invalid input parameter"),

  USER_DUPLICATE_BY_EMAIL("USER-40000", "Duplicated user by email"),
  USER_DUPLICATE_BY_NICKNAME("USER-40001", "Duplicated user by nickname");

  ;


  fun getCode(): String {
    return this.code
  }

  fun getMessage(): String {
    return this.message
  }
}

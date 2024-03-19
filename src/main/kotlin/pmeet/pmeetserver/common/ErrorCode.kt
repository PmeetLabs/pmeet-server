package pmeet.pmeetserver.common

enum class ErrorCode(private val code: String, private val message: String) {
  INVALID_PARAMETER("40001", "invalid input parameter");

  fun getCode(): String {
    return this.code
  }

  fun getMessage(): String {
    return this.message
  }
}

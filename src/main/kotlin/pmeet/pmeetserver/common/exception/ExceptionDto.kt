package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

data class ExceptionDto(val errorCode: ErrorCode, val message: String)


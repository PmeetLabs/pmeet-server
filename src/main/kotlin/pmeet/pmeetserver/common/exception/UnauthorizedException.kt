package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

class UnauthorizedException(val errorCode: ErrorCode) : RuntimeException(errorCode.getMessage())

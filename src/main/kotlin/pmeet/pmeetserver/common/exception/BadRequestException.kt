package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

class BadRequestException(val errorCode: ErrorCode) : RuntimeException(errorCode.getMessage())


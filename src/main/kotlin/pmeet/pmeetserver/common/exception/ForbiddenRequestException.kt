package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

class ForbiddenRequestException(val errorCode: ErrorCode) : RuntimeException(errorCode.getMessage()) {}

package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

class EntityNotFoundException(val errorCode: ErrorCode) : RuntimeException(errorCode.getMessage()) {
}

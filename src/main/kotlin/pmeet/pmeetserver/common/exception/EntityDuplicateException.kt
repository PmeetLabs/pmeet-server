package pmeet.pmeetserver.common.exception

import pmeet.pmeetserver.common.ErrorCode

class EntityDuplicateException(val errorCode: ErrorCode) : RuntimeException(errorCode.getMessage())

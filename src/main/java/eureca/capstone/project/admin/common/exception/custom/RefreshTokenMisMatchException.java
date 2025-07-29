package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class RefreshTokenMisMatchException extends CustomException {
    public RefreshTokenMisMatchException() { super(ErrorCode.REFRESH_TOKEN_MISMATCH); }
}

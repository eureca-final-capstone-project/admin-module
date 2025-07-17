package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class RestrictionTargetNotFoundException extends CustomException {
    public RestrictionTargetNotFoundException() { super(ErrorCode.RESTRICTION_TARGET_NOT_FOUND); }
}

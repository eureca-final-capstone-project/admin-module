package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class RestrictionTypeNotFoundException extends CustomException {
    public RestrictionTypeNotFoundException() { super(ErrorCode.RESTRICTION_TYPE_NOT_FOUND); }
}

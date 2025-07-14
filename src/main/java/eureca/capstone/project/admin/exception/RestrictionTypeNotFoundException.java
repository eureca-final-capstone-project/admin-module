package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class RestrictionTypeNotFoundException extends CustomException {
    public RestrictionTypeNotFoundException() { super(ErrorCode.RESTRICTION_TYPE_NOT_FOUND); }
}

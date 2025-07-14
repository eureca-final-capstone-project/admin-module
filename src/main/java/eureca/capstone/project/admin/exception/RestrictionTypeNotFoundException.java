package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;

public class RestrictionTypeNotFoundException extends CustomException {
    public RestrictionTypeNotFoundException() { super(ErrorMessages.RESTRICTION_TYPE_NOT_FOUND); }
}

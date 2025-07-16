package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class StatusNotFoundException extends CustomException {
    public StatusNotFoundException() { super(ErrorCode.STATUS_NOT_FOUND); }
}

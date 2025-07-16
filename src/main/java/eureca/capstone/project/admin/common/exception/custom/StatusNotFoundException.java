package eureca.capstone.project.admin.common.exception.custom;


import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class StatusNotFoundException extends CustomException {
    public StatusNotFoundException() { super(ErrorCode.STATUS_NOT_FOUND); }
}

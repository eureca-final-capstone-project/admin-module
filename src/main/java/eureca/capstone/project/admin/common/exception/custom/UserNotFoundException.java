package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class UserNotFoundException extends CustomException{
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}

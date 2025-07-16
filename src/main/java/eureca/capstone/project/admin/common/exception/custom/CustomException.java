package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

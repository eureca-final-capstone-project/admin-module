package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;
import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException {
    private final ErrorMessages errorCode;

    public CustomException(ErrorMessages errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

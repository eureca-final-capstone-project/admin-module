package eureca.capstone.project.admin.exception.handler;

import eureca.capstone.project.admin.exception.*;
import eureca.capstone.project.admin.response.ApiResponse;
import eureca.capstone.project.admin.response.ErrorMessages;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleReportNotFoundException(ReportNotFoundException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(ReportTypeNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleReportTypeNotFoundException(ReportTypeNotFoundException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(AlreadyProcessedReportException.class)
    protected ResponseEntity<ApiResponse<?>> handleAlreadyProcessedReportException(AlreadyProcessedReportException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(DuplicateReportException.class)
    protected ResponseEntity<ApiResponse<?>> handleDuplicateReportException(DuplicateReportException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(RestrictionTypeNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleRestrictionTypeNotFoundException(RestrictionTypeNotFoundException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(AiReviewException.class)
    protected ResponseEntity<ApiResponse<?>> handleAiReviewException(AiReviewException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String message = String.format("'%s' 파라미터에 잘못된 값이 들어왔습니다: '%s'", paramName, value);

        return ResponseEntity
                .status(ErrorMessages.INVALID_ENUM_VALUE.getHttpStatus())
                .body(ApiResponse.fail(
                        ErrorMessages.INVALID_ENUM_VALUE.getHttpStatus().value(),
                        message
                ));
    }


    private ResponseEntity<ApiResponse<?>> handleExceptionInternal(ErrorMessages errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getHttpStatus().value(), errorCode.getMessage()));
    }
}
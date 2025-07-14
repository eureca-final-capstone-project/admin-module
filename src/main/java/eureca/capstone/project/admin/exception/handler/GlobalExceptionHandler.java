package eureca.capstone.project.admin.exception.handler;

import eureca.capstone.project.admin.exception.*;
import eureca.capstone.project.admin.response.ApiResponse;
import eureca.capstone.project.admin.response.ErrorMessages;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Report Exceptions
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

    // Restriction Exceptions
    @ExceptionHandler(RestrictionTypeNotFoundException.class)
    protected ResponseEntity<ApiResponse<?>> handleRestrictionTypeNotFoundException(RestrictionTypeNotFoundException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    // External Service Exceptions
    @ExceptionHandler(AiReviewException.class)
    protected ResponseEntity<ApiResponse<?>> handleAiReviewException(AiReviewException e) {
        return handleExceptionInternal(e.getErrorCode());
    }

    // 공통 처리 메서드
    private ResponseEntity<ApiResponse<?>> handleExceptionInternal(ErrorMessages errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getHttpStatus().value(), errorCode.getMessage()));
    }
}
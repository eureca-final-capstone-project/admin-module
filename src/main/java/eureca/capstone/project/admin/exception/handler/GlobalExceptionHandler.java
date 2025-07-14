package eureca.capstone.project.admin.exception.handler;

import eureca.capstone.project.admin.exception.*;
import eureca.capstone.project.admin.response.BaseResponseDto;
import eureca.capstone.project.admin.response.ErrorCode;
import eureca.capstone.project.admin.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleReportNotFoundException(ReportNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.REPORT_NOT_FOUND);
    }

    @ExceptionHandler(ReportTypeNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleReportTypeNotFoundException(ReportTypeNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.REPORT_TYPE_NOT_FOUND);
    }

    @ExceptionHandler(AlreadyProcessedReportException.class)
    public BaseResponseDto<ErrorResponseDto> handleAlreadyProcessedReportException(AlreadyProcessedReportException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.ALREADY_PROCESSED_REPORT);
    }

    @ExceptionHandler(DuplicateReportException.class)
    public BaseResponseDto<ErrorResponseDto> handleDuplicateReportException(DuplicateReportException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.DUPLICATE_REPORT);
    }

    @ExceptionHandler(RestrictionTypeNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleRestrictionTypeNotFoundException(RestrictionTypeNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.RESTRICTION_TYPE_NOT_FOUND);
    }

    @ExceptionHandler(AiReviewException.class)
    public BaseResponseDto<ErrorResponseDto> handleAiReviewException(AiReviewException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.AI_REVIEW_FAILED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponseDto<ErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.INVALID_ENUM_VALUE);
    }
}
package eureca.capstone.project.admin.common.exception;

import eureca.capstone.project.admin.common.exception.custom.*;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.common.exception.code.ErrorCode;
import eureca.capstone.project.admin.common.dto.base.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @ExceptionHandler(UserNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.USER_NOT_FOUND);
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

    @ExceptionHandler(StatusNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleAiReviewException(StatusNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.STATUS_NOT_FOUND);
    }

    @ExceptionHandler(AlreadyProcessedRestrictionException.class)
    public BaseResponseDto<ErrorResponseDto> handleAlreadyProcessedRestrictionExceptionn(AlreadyProcessedRestrictionException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.ALREADY_PROCESSED_RESTRICTION);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponseDto<ErrorResponseDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.INVALID_ENUM_VALUE);
    }

    @ExceptionHandler(RestrictionTargetNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleRestrictionTargetNotFoundException(RestrictionTargetNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.RESTRICTION_TARGET_NOT_FOUND);
    }

    @ExceptionHandler(TransactionFeedNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleTransactionFeedNotFoundException(TransactionFeedNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.TRANSACTION_FEED_NOT_FOUND);
    }

    @ExceptionHandler(SalesTypeNotFoundException.class)
    public BaseResponseDto<ErrorResponseDto> handleSalesTypeNotFoundException(SalesTypeNotFoundException e) {
        log.error(e.getMessage(), e);
        return BaseResponseDto.fail(ErrorCode.SALES_TYPE_NOT_FOUND);
    }
}
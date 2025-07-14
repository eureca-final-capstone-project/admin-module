package eureca.capstone.project.admin.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorMessages {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

    // Report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 내역을 찾을 수 없습니다."),
    REPORT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 유형을 찾을 수 없습니다."),
    ALREADY_PROCESSED_REPORT(HttpStatus.BAD_REQUEST, "이미 처리된 신고입니다."),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "이미 신고한 사용자의 게시글입니다."),

    // Restriction
    RESTRICTION_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "제재 유형을 찾을 수 없습니다."),

    // External
    TRANSACTION_FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    AI_REVIEW_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 검토에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus httpStatus;
    private final String message;
}

package eureca.capstone.project.admin.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_ENUM_VALUE(70001, "INVALID_ENUM_VALUE", "유효하지 않은 요청 값입니다."), // <-- 추가된 코드
    ALREADY_PROCESSED_REPORT(70002, "ALREADY_PROCESSED_REPORT", "이미 처리된 신고입니다."),

    REPORT_NOT_FOUND(70003, "REPORT_NOT_FOUND", "신고 내역을 찾을 수 없습니다."),
    REPORT_TYPE_NOT_FOUND(70004, "REPORT_TYPE_NOT_FOUND", "신고 유형을 찾을 수 없습니다."),
    RESTRICTION_TYPE_NOT_FOUND(70005, "RESTRICTION_TYPE_NOT_FOUND", "제재 유형을 찾을 수 없습니다."),
    TRANSACTION_FEED_NOT_FOUND(70006, "TRANSACTION_FEED_NOT_FOUND", "게시글을 찾을 수 없습니다."),

    DUPLICATE_REPORT(70007, "DUPLICATE_REPORT", "이미 신고한 사용자의 게시글입니다."),

    AI_REVIEW_FAILED(70008, "AI_REVIEW_FAILED", "AI 검토에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final int code;
    private final String name;
    private final String message;
}
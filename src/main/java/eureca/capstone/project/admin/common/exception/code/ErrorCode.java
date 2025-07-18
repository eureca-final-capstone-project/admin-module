package eureca.capstone.project.admin.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // auth 관련 에러코드 (10000 ~ 19999)
    UNKNOWN_ERROR(10000, "UNKNOWN_ERROR", "알수없는 에러"),
    TOKEN_EXPIRED(10001, "TOKEN_EXPIRED", "Access Token 만료"),
    INVALID_SIGNATURE(10002, "INVALID_SIGNATURE", "JWT 서명 오류"),
    MALFORMED_TOKEN(10003, "MALFORMED_TOKEN", "JWT 구조 오류"),
    MISSING_TOKEN(10004, "MISSING_TOKEN", "JWT 누락"),
    REFRESH_TOKEN_MISMATCH(10005, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Refresh Token, 요청값의 Refresh Token 값 불일치"),
    EMAIL_TOKEN_MISMATCH(10006, "REFRESH_TOKEN_MISMATCH", "Redis 에 저장된 Email Token, 요청값의 Email Token 값 불일치"),
    BLACK_LIST_USER_FOUND(10007, "BLACK_LIST_USER_FOUND", "해당 사용자는 BlackList 포함된 사용자 입니다."),

    // admin 관련 에러코드 (70000~79999)
    INVALID_ENUM_VALUE(70001, "INVALID_ENUM_VALUE", "유효하지 않은 요청 값입니다."), // <-- 추가된 코드
    ALREADY_PROCESSED_REPORT(70002, "ALREADY_PROCESSED_REPORT", "이미 처리된 신고입니다."),

    REPORT_NOT_FOUND(70003, "REPORT_NOT_FOUND", "신고 내역을 찾을 수 없습니다."),
    REPORT_TYPE_NOT_FOUND(70004, "REPORT_TYPE_NOT_FOUND", "신고 유형을 찾을 수 없습니다."),
    RESTRICTION_TYPE_NOT_FOUND(70005, "RESTRICTION_TYPE_NOT_FOUND", "제재 유형을 찾을 수 없습니다."),
    TRANSACTION_FEED_NOT_FOUND(70006, "TRANSACTION_FEED_NOT_FOUND", "게시글을 찾을 수 없습니다."),
    RESTRICTION_TARGET_NOT_FOUND(70011, "RESTRICTION_TARGET_NOT_FOUND", "제재 대상을 찾을 수 없습니다."),
    ALREADY_PROCESSED_RESTRICTION(70012, "ALREADY_PROCESSED_RESTRICTION", "이미 처리된 제재입니다."),

    DUPLICATE_REPORT(70007, "DUPLICATE_REPORT", "이미 신고한 사용자의 게시글입니다."),

    AI_REVIEW_FAILED(70008, "AI_REVIEW_FAILED", "AI 검토에 실패했습니다. 잠시 후 다시 시도해 주세요."),
    STATUS_NOT_FOUND(70009, "STATUS_NOT_FOUND","상태값을 찾을 수 없습니다"),
    USER_NOT_FOUND(70010, "USER_NOT_FOUND", "유저를 찾을 수 없습니다.");

    private final int code;
    private final String name;
    private final String message;
}
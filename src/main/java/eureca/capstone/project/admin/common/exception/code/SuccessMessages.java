package eureca.capstone.project.admin.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessMessages {

    GET_REPORT_COUNTS_SUCCESS(HttpStatus.OK, "신고 건수 조회를 성공했습니다."),
    GET_REPORT_HISTORY_SUCCESS(HttpStatus.OK, "신고 내역 목록 조회를 성공했습니다."),
    GET_RESTRICTION_LIST_SUCCESS(HttpStatus.OK, "제재 내역 목록 조회를 성공했습니다."),
    PROCESS_REPORT_SUCCESS(HttpStatus.OK, "신고 처리를 성공했습니다."),
    CREATE_REPORT_SUCCESS(HttpStatus.ACCEPTED, "신고 접수를 성공했습니다."), // 비동기 처리
    GET_EXPIRED_RESTRICTIONS_SUCCESS(HttpStatus.OK, "만료된 제재 목록 조회를 성공했습니다."),
    UPDATE_RESTRICTION_STATUS_SUCCESS(HttpStatus.OK, "제재 상태 변경을 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode(){
        return this.httpStatus.value();
    }
}

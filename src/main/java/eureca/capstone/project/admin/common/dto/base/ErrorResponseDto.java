package eureca.capstone.project.admin.common.dto.base;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponseDto {
    private final int statusCode;
    private final String statusCodeName;
    private final String detailMessage;

    public static ErrorResponseDto of(ErrorCode errorCode) {
        return ErrorResponseDto.builder()
                .statusCode(errorCode.getCode())
                .statusCodeName(errorCode.getName())
                .detailMessage(errorCode.getMessage())
                .build();
    }
}

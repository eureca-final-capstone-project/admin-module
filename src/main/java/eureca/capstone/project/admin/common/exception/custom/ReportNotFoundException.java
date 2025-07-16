package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class ReportNotFoundException extends CustomException {
    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }
}

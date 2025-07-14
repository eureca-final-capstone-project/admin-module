package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class ReportNotFoundException extends CustomException {
    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }
}

package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class ReportTypeNotFoundException extends CustomException {
    public ReportTypeNotFoundException() { super(ErrorCode.REPORT_TYPE_NOT_FOUND); }
}

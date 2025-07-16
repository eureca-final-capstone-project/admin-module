package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class ReportTypeNotFoundException extends CustomException {
    public ReportTypeNotFoundException() { super(ErrorCode.REPORT_TYPE_NOT_FOUND); }
}

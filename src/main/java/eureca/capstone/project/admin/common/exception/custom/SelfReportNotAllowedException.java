package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class SelfReportNotAllowedException extends CustomException {
    public SelfReportNotAllowedException() { super(ErrorCode.SELF_REPORT_NOT_ALLOWED); }
}

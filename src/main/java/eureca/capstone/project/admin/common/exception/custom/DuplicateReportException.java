package eureca.capstone.project.admin.common.exception.custom;


import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class DuplicateReportException extends CustomException {
    public DuplicateReportException() {
        super(ErrorCode.DUPLICATE_REPORT);
    }
}

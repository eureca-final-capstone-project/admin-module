package eureca.capstone.project.admin.exception;


import eureca.capstone.project.admin.response.ErrorCode;

public class DuplicateReportException extends CustomException {
    public DuplicateReportException() {
        super(ErrorCode.DUPLICATE_REPORT);
    }
}

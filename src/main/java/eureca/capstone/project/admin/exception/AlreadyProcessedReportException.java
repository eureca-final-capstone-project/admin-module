package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class AlreadyProcessedReportException extends CustomException {
    public AlreadyProcessedReportException() { super(ErrorCode.ALREADY_PROCESSED_REPORT); }
}

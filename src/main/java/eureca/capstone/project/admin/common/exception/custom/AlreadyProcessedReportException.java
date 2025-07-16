package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class AlreadyProcessedReportException extends CustomException {
    public AlreadyProcessedReportException() { super(ErrorCode.ALREADY_PROCESSED_REPORT); }
}

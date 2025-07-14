package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;

public class AlreadyProcessedReportException extends CustomException {
    public AlreadyProcessedReportException() { super(ErrorMessages.ALREADY_PROCESSED_REPORT); }
}

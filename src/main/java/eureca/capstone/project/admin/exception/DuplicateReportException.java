package eureca.capstone.project.admin.exception;


import eureca.capstone.project.admin.response.ErrorMessages;

public class DuplicateReportException extends CustomException {
    public DuplicateReportException() {
        super(ErrorMessages.DUPLICATE_REPORT);
    }
}

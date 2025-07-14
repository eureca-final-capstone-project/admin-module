package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;

public class ReportNotFoundException extends CustomException {
    public ReportNotFoundException() {
        super(ErrorMessages.REPORT_NOT_FOUND);
    }
}

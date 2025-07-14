package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;

public class ReportTypeNotFoundException extends CustomException {
    public ReportTypeNotFoundException() { super(ErrorMessages.REPORT_TYPE_NOT_FOUND); }
}

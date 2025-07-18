package eureca.capstone.project.admin.common.exception.custom;


import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class AlreadyProcessedRestrictionException extends CustomException {
    public AlreadyProcessedRestrictionException() { super(ErrorCode.ALREADY_PROCESSED_RESTRICTION); }
}

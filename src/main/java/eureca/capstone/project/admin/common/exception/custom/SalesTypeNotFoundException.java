package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class SalesTypeNotFoundException extends CustomException {
    public SalesTypeNotFoundException() {
        super(ErrorCode.SALES_TYPE_NOT_FOUND);
    }
}

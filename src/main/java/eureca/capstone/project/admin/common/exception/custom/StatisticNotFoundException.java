package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class StatisticNotFoundException extends CustomException {
    public StatisticNotFoundException() {
        super(ErrorCode.STATISTIC_NOT_FOUND);
    }
}

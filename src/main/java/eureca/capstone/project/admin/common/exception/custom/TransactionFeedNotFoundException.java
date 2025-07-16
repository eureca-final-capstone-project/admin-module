package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class TransactionFeedNotFoundException extends CustomException{
    public TransactionFeedNotFoundException() {
        super(ErrorCode.TRANSACTION_FEED_NOT_FOUND);
    }
}

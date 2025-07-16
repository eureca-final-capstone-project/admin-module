package eureca.capstone.project.admin.common.exception.custom;

import eureca.capstone.project.admin.common.exception.code.ErrorCode;

public class AiReviewException extends CustomException {
    public AiReviewException() { super(ErrorCode.AI_REVIEW_FAILED); }
}

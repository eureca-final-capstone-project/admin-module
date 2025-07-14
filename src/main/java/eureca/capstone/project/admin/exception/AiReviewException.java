package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorCode;

public class AiReviewException extends CustomException {
    public AiReviewException() { super(ErrorCode.AI_REVIEW_FAILED); }
}

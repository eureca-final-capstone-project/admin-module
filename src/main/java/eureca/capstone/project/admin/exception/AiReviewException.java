package eureca.capstone.project.admin.exception;

import eureca.capstone.project.admin.response.ErrorMessages;

public class AiReviewException extends CustomException {
    public AiReviewException() { super(ErrorMessages.AI_REVIEW_FAILED); }
}

package org.example.polify.attempt;

public class QuestionNotFoundException extends RuntimeException {
    private final long questionId;

    public QuestionNotFoundException(long questionId) {
        super("Question not found: " + questionId);
        this.questionId = questionId;
    }

    public long getQuestionId() {
        return questionId;
    }
}


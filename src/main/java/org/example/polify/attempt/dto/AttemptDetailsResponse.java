package org.example.polify.attempt.dto;

import java.time.Instant;
import java.util.List;

public class AttemptDetailsResponse {
    private final long attemptId;
    private final long surveyId;
    private final String status;
    private final Instant startedAt;
    private final Instant completedAt;
    private final List<AnswerDto> answers;
    private final Long nextQuestionId;

    public AttemptDetailsResponse(
        long attemptId,
        long surveyId,
        String status,
        Instant startedAt,
        Instant completedAt,
        List<AnswerDto> answers,
        Long nextQuestionId
    ) {
        this.attemptId = attemptId;
        this.surveyId = surveyId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.answers = answers;
        this.nextQuestionId = nextQuestionId;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public long getSurveyId() {
        return surveyId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<AnswerDto> getAnswers() {
        return answers;
    }

    public Long getNextQuestionId() {
        return nextQuestionId;
    }

    public static class AnswerDto {
        private final long answerId;
        private final long questionId;
        private final Instant answeredAt;
        private final String textValue;
        private final List<Long> optionIds;
        private final List<PriorityDto> priority;

        public AnswerDto(
            long answerId,
            long questionId,
            Instant answeredAt,
            String textValue,
            List<Long> optionIds,
            List<PriorityDto> priority
        ) {
            this.answerId = answerId;
            this.questionId = questionId;
            this.answeredAt = answeredAt;
            this.textValue = textValue;
            this.optionIds = optionIds;
            this.priority = priority;
        }

        public long getAnswerId() {
            return answerId;
        }

        public long getQuestionId() {
            return questionId;
        }

        public Instant getAnsweredAt() {
            return answeredAt;
        }

        public String getTextValue() {
            return textValue;
        }

        public List<Long> getOptionIds() {
            return optionIds;
        }

        public List<PriorityDto> getPriority() {
            return priority;
        }
    }

    public static class PriorityDto {
        private final long optionId;
        private final int rank;

        public PriorityDto(long optionId, int rank) {
            this.optionId = optionId;
            this.rank = rank;
        }

        public long getOptionId() {
            return optionId;
        }

        public int getRank() {
            return rank;
        }
    }
}


package org.example.polify.attempt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Attempt details including saved answers and nextQuestionId for resume UX.")
public class AttemptDetailsResponse {
    @Schema(description = "Attempt id.", example = "5")
    private final long attemptId;
    @Schema(description = "Survey id.", example = "1")
    private final long surveyId;
    @Schema(description = "Attempt status: IN_PROGRESS | COMPLETED | ABANDONED.", example = "IN_PROGRESS")
    private final String status;
    @Schema(description = "Attempt started at (UTC).", example = "2026-05-03T10:00:00Z")
    private final Instant startedAt;
    @Schema(description = "Attempt completed at (UTC). Null until completed.", nullable = true)
    private final Instant completedAt;
    @Schema(description = "Saved answers (may be empty in list view).")
    private final List<AnswerDto> answers;
    @Schema(description = "First required question not answered yet (by position). Null if all required are answered.", nullable = true)
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
        @Schema(description = "Answer header id.", example = "100")
        private final long answerId;
        @Schema(description = "Question id.", example = "101")
        private final long questionId;
        @Schema(description = "When the answer was last submitted (UTC).", example = "2026-05-03T10:05:00Z")
        private final Instant answeredAt;
        @Schema(description = "TEXT value (if applicable).", nullable = true)
        private final String textValue;
        @Schema(description = "Chosen option ids (RADIO/SELECT/CHECKBOX).", nullable = true)
        private final List<Long> optionIds;
        @Schema(description = "Priority items (PRIORITY).", nullable = true)
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
        @Schema(description = "Option id.", example = "1001")
        private final long optionId;
        @Schema(description = "Rank starting at 1.", example = "1")
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

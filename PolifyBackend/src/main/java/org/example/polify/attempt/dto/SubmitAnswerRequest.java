package org.example.polify.attempt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(
    description = "Submit/update one answer within an attempt. Payload must match the question type. " +
        "TEXT uses textValue; RADIO/SELECT uses optionId; CHECKBOX uses optionIds; PRIORITY uses priority list."
)
public class SubmitAnswerRequest {
    @Positive
    @Schema(description = "Question id being answered.", example = "101")
    private long questionId;

    @Size(max = 500)
    @Schema(description = "TEXT value (max 500). Only for TEXT questions.", example = "Coffee")
    private String textValue;

    // For RADIO/SELECT
    @Schema(description = "Single option id (RADIO/SELECT).", example = "1001", nullable = true)
    private Long optionId;

    // For CHECKBOX
    @Schema(description = "Multiple option ids (CHECKBOX).", example = "[1001,1002]", nullable = true)
    private List<Long> optionIds;

    @Schema(description = "Priority list (PRIORITY): each item has optionId + rank. Ranks start at 1.", nullable = true)
    private List<PriorityItem> priority;

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public List<Long> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(List<Long> optionIds) {
        this.optionIds = optionIds;
    }

    public List<PriorityItem> getPriority() {
        return priority;
    }

    public void setPriority(List<PriorityItem> priority) {
        this.priority = priority;
    }

    public static class PriorityItem {
        @NotNull
        @Schema(description = "Option id.", example = "1001")
        private Long optionId;

        @NotNull
        @Schema(description = "Rank (1..N).", example = "1")
        private Integer rank;

        public Long getOptionId() {
            return optionId;
        }

        public void setOptionId(Long optionId) {
            this.optionId = optionId;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }
}

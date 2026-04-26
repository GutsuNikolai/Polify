package org.example.polify.attempt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SubmitAnswerRequest {
    @Positive
    private long questionId;

    @Size(max = 500)
    private String textValue;

    // For RADIO/SELECT
    private Long optionId;

    // For CHECKBOX
    private List<Long> optionIds;

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
        private Long optionId;

        @NotNull
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

package org.example.polify.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.example.polify.survey.QuestionType;

@Schema(description = "Create a new survey (moderator only). Surveys are immutable after creation in MVP.")
public class CreateSurveyRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @Min(0)
    @Max(9900)
    private int rewardAmountBani;

    @Min(1)
    private int targetCompletions;

    @NotEmpty
    @Valid
    private List<CreateQuestion> questions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRewardAmountBani() {
        return rewardAmountBani;
    }

    public void setRewardAmountBani(int rewardAmountBani) {
        this.rewardAmountBani = rewardAmountBani;
    }

    public int getTargetCompletions() {
        return targetCompletions;
    }

    public void setTargetCompletions(int targetCompletions) {
        this.targetCompletions = targetCompletions;
    }

    public List<CreateQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuestion> questions) {
        this.questions = questions;
    }

    public static class CreateQuestion {
        @NotNull
        private QuestionType type;

        @NotBlank
        @Size(max = 500)
        private String text;

        @Min(1)
        private int position;

        private boolean required;

        @Valid
        private List<CreateOption> options;

        public QuestionType getType() {
            return type;
        }

        public void setType(QuestionType type) {
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public List<CreateOption> getOptions() {
            return options;
        }

        public void setOptions(List<CreateOption> options) {
            this.options = options;
        }
    }

    public static class CreateOption {
        @NotBlank
        @Size(max = 200)
        private String label;

        @NotBlank
        @Size(max = 200)
        private String value;

        @Min(1)
        private int position;

        @Size(max = 2000)
        private String mediaUrl;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getMediaUrl() {
            return mediaUrl;
        }

        public void setMediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
        }
    }
}


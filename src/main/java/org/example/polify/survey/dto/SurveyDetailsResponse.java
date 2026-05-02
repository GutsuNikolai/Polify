package org.example.polify.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Survey with full questions/options structure.")
public class SurveyDetailsResponse {
    @Schema(description = "Survey id.", example = "1")
    private final Long id;
    @Schema(description = "Title.", example = "E2E All Types")
    private final String title;
    @Schema(description = "Description.", example = "Survey containing all supported question types.")
    private final String description;
    @Schema(description = "Reward amount in bani.", example = "300")
    private final int rewardAmountBani;
    @Schema(description = "Target number of completions.", example = "10")
    private final int targetCompletions;
    @Schema(description = "Ordered list of questions.")
    private final List<QuestionDto> questions;

    public SurveyDetailsResponse(
        Long id,
        String title,
        String description,
        int rewardAmountBani,
        int targetCompletions,
        List<QuestionDto> questions
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rewardAmountBani = rewardAmountBani;
        this.targetCompletions = targetCompletions;
        this.questions = questions;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getRewardAmountBani() {
        return rewardAmountBani;
    }

    public int getTargetCompletions() {
        return targetCompletions;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public static class QuestionDto {
        @Schema(description = "Question id.", example = "123")
        private final Long id;
        @Schema(description = "Question type: TEXT | RADIO | CHECKBOX | SELECT | PRIORITY.", example = "RADIO")
        private final String type;
        @Schema(description = "Question text.", example = "How many hours do you sleep on average?")
        private final String text;
        @Schema(description = "Position within survey (1..N).", example = "2")
        private final int position;
        @Schema(description = "Whether answering is required.", example = "true")
        private final boolean required;
        @Schema(description = "Options for non-TEXT question types (may be empty for TEXT).")
        private final List<OptionDto> options;

        public QuestionDto(Long id, String type, String text, int position, boolean required, List<OptionDto> options) {
            this.id = id;
            this.type = type;
            this.text = text;
            this.position = position;
            this.required = required;
            this.options = options;
        }

        public Long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public int getPosition() {
            return position;
        }

        public boolean isRequired() {
            return required;
        }

        public List<OptionDto> getOptions() {
            return options;
        }
    }

    public static class OptionDto {
        @Schema(description = "Option id.", example = "10")
        private final Long id;
        @Schema(description = "Option label.", example = "6-7")
        private final String label;
        @Schema(description = "Stable option value.", example = "6_7")
        private final String value;
        @Schema(description = "Position within options list.", example = "2")
        private final int position;
        @Schema(description = "Optional media URL.", example = "https://cdn.example.com/img.png", nullable = true)
        private final String mediaUrl;

        public OptionDto(Long id, String label, String value, int position, String mediaUrl) {
            this.id = id;
            this.label = label;
            this.value = value;
            this.position = position;
            this.mediaUrl = mediaUrl;
        }

        public Long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public int getPosition() {
            return position;
        }

        public String getMediaUrl() {
            return mediaUrl;
        }
    }
}

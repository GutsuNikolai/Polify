package org.example.polify.survey.dto;

import java.util.List;

public class SurveyDetailsResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final int rewardAmountBani;
    private final int targetCompletions;
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
        private final Long id;
        private final String type;
        private final String text;
        private final int position;
        private final boolean required;
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
        private final Long id;
        private final String label;
        private final String value;
        private final int position;
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


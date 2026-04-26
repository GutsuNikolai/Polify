package org.example.polify.survey.dto;

public class SurveyListItem {
    private final Long id;
    private final String title;
    private final String description;
    private final int rewardAmountBani;
    private final int targetCompletions;

    public SurveyListItem(Long id, String title, String description, int rewardAmountBani, int targetCompletions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.rewardAmountBani = rewardAmountBani;
        this.targetCompletions = targetCompletions;
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
}


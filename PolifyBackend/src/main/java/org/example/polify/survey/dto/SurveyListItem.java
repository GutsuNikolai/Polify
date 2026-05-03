package org.example.polify.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Survey list item (short info).")
public class SurveyListItem {
    @Schema(description = "Survey id.", example = "1")
    private final Long id;
    @Schema(description = "Title.", example = "Morning habits")
    private final String title;
    @Schema(description = "Short description.", example = "Quick survey about your routine.")
    private final String description;
    @Schema(description = "Reward amount in bani (0..9900).", example = "250")
    private final int rewardAmountBani;
    @Schema(description = "Target number of completions.", example = "1000")
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

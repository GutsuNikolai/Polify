package org.example.polify.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Survey list item for moderator management (includes archived flag).")
public record SurveyManageListItem(
    @Schema(description = "Survey id.", example = "1") long id,
    @Schema(description = "Survey title.", example = "Daily habits") String title,
    @Schema(description = "Archived flag.", example = "false") boolean archived,
    @Schema(description = "Archived at (UTC) when archived, otherwise null.", example = "2026-05-19T10:10:00Z") Instant archivedAt
) {}


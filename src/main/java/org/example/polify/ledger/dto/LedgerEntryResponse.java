package org.example.polify.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Ledger entry (reward) for a completed attempt.")
public class LedgerEntryResponse {
    @Schema(description = "Ledger entry id.", example = "200")
    private final long id;
    @Schema(description = "Attempt id.", example = "5")
    private final long attemptId;
    @Schema(description = "User id.", example = "1")
    private final long userId;
    @Schema(description = "Amount in bani.", example = "300")
    private final int amountBani;
    @Schema(description = "Currency (fixed).", example = "MDL")
    private final String currency;
    @Schema(description = "Ledger status: CREATED | CONFIRMED | FAILED.", example = "CREATED")
    private final String status;
    @Schema(description = "Created at (UTC).", example = "2026-05-03T10:10:00Z")
    private final Instant createdAt;

    public LedgerEntryResponse(
        long id,
        long attemptId,
        long userId,
        int amountBani,
        String currency,
        String status,
        Instant createdAt
    ) {
        this.id = id;
        this.attemptId = attemptId;
        this.userId = userId;
        this.amountBani = amountBani;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public long getUserId() {
        return userId;
    }

    public int getAmountBani() {
        return amountBani;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package org.example.polify.ledger.dto;

import java.time.Instant;

public class LedgerEntryResponse {
    private final long id;
    private final long attemptId;
    private final long userId;
    private final int amountBani;
    private final String currency;
    private final String status;
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


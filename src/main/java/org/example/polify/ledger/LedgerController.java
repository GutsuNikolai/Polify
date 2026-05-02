package org.example.polify.ledger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.example.polify.auth.PolifyPrincipal;
import org.example.polify.common.error.ApiError;
import org.example.polify.ledger.dto.LedgerEntryResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ledger")
@Tag(name = "Ledger", description = "Rewards ledger (read-only in MVP).")
@SecurityRequirement(name = "bearerAuth")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    @Operation(summary = "List ledger entries", description = "Lists ledger entries for current user (optional filter by attemptId).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LedgerEntryResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<LedgerEntryResponse> list(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @RequestParam(required = false) Long attemptId
    ) {
        return ledgerService.list(principal.userId(), attemptId);
    }
}

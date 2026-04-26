package org.example.polify.ledger;

import java.util.List;
import org.example.polify.auth.PolifyPrincipal;
import org.example.polify.ledger.dto.LedgerEntryResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ledger")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public List<LedgerEntryResponse> list(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @RequestParam(required = false) Long attemptId
    ) {
        return ledgerService.list(principal.userId(), attemptId);
    }
}


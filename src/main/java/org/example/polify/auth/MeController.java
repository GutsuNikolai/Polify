package org.example.polify.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MeController {
    @GetMapping
    public PolifyPrincipal me(@AuthenticationPrincipal PolifyPrincipal principal) {
        return principal;
    }
}


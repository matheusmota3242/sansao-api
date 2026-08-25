package dev.m2g2.simao.controller.management;

import dev.m2g2.simao.dto.management.MeResponse;
import dev.m2g2.simao.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return MeResponse.from(principal.getUser());
    }
}

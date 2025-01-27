package com.springbit.gateway.controller;

import com.springbit.gateway.controller.dto.UserInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    //@PreAuthorize("hasAuthority('SCOPE_read:user')")
    @GetMapping("/info")
    public Mono<UserInfo> info(@AuthenticationPrincipal  OAuth2User oAuth2User) {
        var attributes = oAuth2User.getAttributes();
        String username = oAuth2User.getAttribute("login");
        if (username == null) {
            username = oAuth2User.getAttribute("name");
        }
        String avatarUrl = oAuth2User.getAttribute("avatar_url");
        if (avatarUrl == null) {
            Map<String, Object> pictureData = oAuth2User.getAttribute("picture");
            if (pictureData != null) {
                pictureData = (Map<String, Object>)pictureData.get("data");
                avatarUrl = (String)pictureData.get("url");
            }
        }
        return Mono.just(new UserInfo(username, avatarUrl));
    }

    // ... other endpoints
}
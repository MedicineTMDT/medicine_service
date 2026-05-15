package com.ryo.identity.configuration;

import com.ryo.identity.constant.Role;
import com.ryo.identity.entity.User;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @NonFinal
    @Value("${frontend.url:url}")
    protected String frontendUrl;

    UserRepository userRepository;
    AuthenticationServiceImpl authenticationService;
    OAuth2AuthorizedClientService authorizedClientService; // ✅ inject qua constructor (Lombok)

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        String googleAccessToken = authorizedClient.getAccessToken().getTokenValue();
        String googleRefreshToken = authorizedClient.getRefreshToken() != null
                ? authorizedClient.getRefreshToken().getTokenValue()
                : null;

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setUsername(generateUniqueUsername(email));
            newUser.setAvatarImg("");
            newUser.setVerifyEmail(true);
            newUser.setPassword("");
            newUser.setRole(Role.USER);
            return userRepository.save(newUser);
        });

        user.setGoogleAccessToken(googleAccessToken);
        if (googleRefreshToken != null) {
            user.setGoogleRefreshToken(googleRefreshToken);
        }
        userRepository.save(user);

        String token = authenticationService.generateTokenFromEmail(email);
        getRedirectStrategy().sendRedirect(request, response,
                frontendUrl + "/oauth2/success?token=" + token);
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int index = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + index;
            index++;
        }
        return username;
    }
}
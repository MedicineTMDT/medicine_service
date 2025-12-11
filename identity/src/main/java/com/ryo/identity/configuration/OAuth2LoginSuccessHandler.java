package com.ryo.identity.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryo.identity.constant.Role;
import com.ryo.identity.dto.response.AuthenticationResponse;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
// /oauth2/authorization/google
// trả về /login/oauth2/code/google
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @NonFinal
    @Value("${frontend.url}")
    protected String frontendUrl;

    UserRepository userRepository;
    AuthenticationServiceImpl authenticationService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // 1️⃣ Nếu user chưa tồn tại → tạo new user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setUsername(generateUniqueUsername(email));
            newUser.setAvatarImg("");
            newUser.setVerifyEmail(true);                   // Google đã verify email
            newUser.setPassword("");                       // Không dùng password
            newUser.setRole(Role.USER);               // Set role
            return userRepository.save(newUser);
        });

        // 2️⃣ Generate JWT của hệ thống bạn
        String token = authenticationService.generateTokenFromEmail(email);

        String redirectUrl = frontendUrl +
                "/oauth2/success?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    // Tạo username không trùng
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

package com.ryo.identity.service;

import com.nimbusds.jose.JOSEException;
import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.IntrospectRequest;
import com.ryo.identity.dto.request.LoginRequest;
import com.ryo.identity.dto.request.LogoutRequest;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;

public interface IAuthenticationService {
    UserResponse createUser(CreateUserRequest request);
    AuthenticationResponse authenticate(LoginRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException;
}

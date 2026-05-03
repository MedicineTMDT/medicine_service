package com.ryo.request.service;

import com.nimbusds.jose.JOSEException;
import com.ryo.request.dto.request.CreateUserRequest;
import com.ryo.request.dto.request.IntrospectRequest;
import com.ryo.request.dto.request.LoginRequest;
import com.ryo.request.dto.request.LogoutRequest;
import com.ryo.request.dto.response.AuthenticationResponse;
import com.ryo.request.dto.response.IntrospectResponse;
import com.ryo.request.dto.response.UserResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    UserResponse createUser(CreateUserRequest request);
    AuthenticationResponse authenticate(LoginRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException;
}

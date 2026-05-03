package com.ryo.prescription.service;

import com.nimbusds.jose.JOSEException;
import com.ryo.prescription.dto.request.CreateUserRequest;
import com.ryo.prescription.dto.request.IntrospectRequest;
import com.ryo.prescription.dto.request.LoginRequest;
import com.ryo.prescription.dto.request.LogoutRequest;
import com.ryo.prescription.dto.response.AuthenticationResponse;
import com.ryo.prescription.dto.response.IntrospectResponse;
import com.ryo.prescription.dto.response.UserResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    UserResponse createUser(CreateUserRequest request);
    AuthenticationResponse authenticate(LoginRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException;
}

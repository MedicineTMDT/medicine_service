package com.ryo.medicine.service;

import com.nimbusds.jose.JOSEException;
import com.ryo.medicine.dto.request.CreateUserRequest;
import com.ryo.medicine.dto.request.IntrospectRequest;
import com.ryo.medicine.dto.request.LoginRequest;
import com.ryo.medicine.dto.request.LogoutRequest;
import com.ryo.medicine.dto.response.AuthenticationResponse;
import com.ryo.medicine.dto.response.IntrospectResponse;
import com.ryo.medicine.dto.response.UserResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    UserResponse createUser(CreateUserRequest request);
    AuthenticationResponse authenticate(LoginRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException;
}

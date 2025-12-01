package com.ryo.identity.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ryo.identity.dto.request.CreateUserRequest;
import com.ryo.identity.dto.request.IntrospectRequest;
import com.ryo.identity.dto.request.LoginRequest;
import com.ryo.identity.dto.request.LogoutRequest;
import com.ryo.identity.dto.response.AuthenticationResponse;
import com.ryo.identity.dto.response.IntrospectResponse;
import com.ryo.identity.dto.response.UserResponse;
import com.ryo.identity.entity.InvalidatedToken;
import com.ryo.identity.entity.User;
import com.ryo.identity.exception.AppException;
import com.ryo.identity.exception.ErrorCode;
import com.ryo.identity.mapper.UserMapper;
import com.ryo.identity.repository.InvalidatedTokenRepository;
import com.ryo.identity.repository.UserRepository;
import com.ryo.identity.service.IAuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service @Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements IAuthenticationService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    UserMapper userMapper;
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request){
        User user = userMapper.createUserRequest2User(request);
        try{
            userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.user2UserResponse(user);
    }

    @Override
    public AuthenticationResponse authenticate(@Valid @RequestBody LoginRequest request)
    {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        String password = request.getPassword();
        var isValid = passwordEncoder.matches(password, user.getPassword());
        if(isValid){
            return AuthenticationResponse.builder()
                    .authenticated(isValid)
                    .token(generateToken(user))
                    .build();
        }else{
            return AuthenticationResponse.builder()
                    .authenticated(isValid)
                    .build();
        }
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException{
        try{
            var signedToken = verifyToken(request.getToken(), false);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = invalidatedTokenRepository.save(
                    InvalidatedToken.builder()
                            .id(jit)
                            .expiryTime(expiryTime)
                            .build()
            );
        }catch (AppException exception){
            log.error("Logout failed");
        }
    }

    @Override
    public IntrospectResponse introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException
    {
        String token = request.getToken();
        boolean isValid = true;
        try{
            verifyToken(token, false);
        }catch (AppException e){
            isValid = false;
        }
        if(!isValid){
            var signedJWT = verifyToken(token, true);
            var jit = signedJWT.getJWTClaimsSet().getJWTID();
            var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);

            var username = signedJWT.getJWTClaimsSet().getSubject();

            var user =
                    userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

            var newToken = generateToken(user);
            isValid = true;
            return IntrospectResponse.builder()
                    .valid(isValid)
                    .token(newToken)
                    .build();
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("devteria.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", user.getRole().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

}

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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    @NonFinal
    @Value("${spring.mail.username}")
    protected String emailAddress;

    UserMapper userMapper;
    JavaMailSender mailSender;
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    @Override
    public UserResponse createUser(CreateUserRequest request){
        User user = userMapper.createUserRequest2User(request);
        log.info("flag1");
        try{
            userRepository.save(user);
            String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
            emailService.sendOtp(user,otp);
            user.setVerifyEmailToken(otp);
            userRepository.save(user);
        }catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        return userMapper.user2UserResponse(user);
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest request)
    {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        if(!user.getVerifyEmail()){
            throw new AppException(ErrorCode.HAVE_NOT_VERIFY_EMAIL);
        }
        String password = request.getPassword();
        var isValid = passwordEncoder.matches(password, user.getPassword());
        if(isValid){
            return AuthenticationResponse.builder()
                    .id(user.getId())
                    .role(user.getRole())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .authenticated(isValid)
                    .token(generateToken(user))
                    .build();
        }else{
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException{
        try{
            var signedToken = verifyToken(request.getToken(), false);
            log.error("Invalidated token in logout");
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            invalidatedTokenRepository.save(
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
    public IntrospectResponse introspect(IntrospectRequest request)
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
            log.info("Token not valid");
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
            log.info("return in 170");
            return IntrospectResponse.builder()
                    .valid(isValid)
                    .token(newToken)
                    .build();
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse verifyForgotPasswordToken(String token, String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!user.getVerifyEmail()){
            throw new AppException(ErrorCode.HAVE_NOT_VERIFY_EMAIL);
        }
        if(user.getForgotPasswordToken().equals(token)){
            return AuthenticationResponse.builder()
                    .id(user.getId())
                    .role(user.getRole())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .token(generateToken(user))
                    .authenticated(true)
                    .build();
        }else
        {
            return AuthenticationResponse.builder()
                    .token("")
                    .authenticated(false)
                    .build();
        }
    }

    public void verifyEmailAddres(String email, String token){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(token.equals(user.getVerifyEmailToken())){
            user.setVerifyEmail(true);
            userRepository.save(user);
        }
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("med.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", user.getRole())
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

    public String generateTokenFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return generateToken(user);
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
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

}

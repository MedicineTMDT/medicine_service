package com.ryo.identity.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1009, "This email is already exist", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1010, "This user is already exist", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_NOT_MATCH(1011, "Confirm password not match", HttpStatus.BAD_REQUEST),
    HAVE_NOT_VERIFY_EMAIL(1012, "This account email hasnt been verifed", HttpStatus.BAD_REQUEST),
    NOT_FOUND(1013, "Resources not found", HttpStatus.BAD_REQUEST),
    SLUG_EXISTS(1014, "This slug is already existed", HttpStatus.BAD_REQUEST),
    DRUG_EXISTS(1015, "This drug is already existed", HttpStatus.BAD_REQUEST),
    DRUG_NOT_EXIST(1016, "Drug not found", HttpStatus.BAD_REQUEST),
    INGREDIENT_NOT_EXIST(1017, "Ingredient not found", HttpStatus.BAD_REQUEST),
    DRUG_INTERACTION_NOT_FOUND(1018, "Drug Interaction not found", HttpStatus.BAD_REQUEST),
    PRESCRIPTION_NOT_FOUND(1019, "Prescription not found", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
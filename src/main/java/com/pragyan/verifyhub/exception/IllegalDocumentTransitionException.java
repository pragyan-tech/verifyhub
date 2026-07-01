package com.pragyan.verifyhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalDocumentTransitionException extends RuntimeException {
    public IllegalDocumentTransitionException(String message) {
        super(message);
    }
}
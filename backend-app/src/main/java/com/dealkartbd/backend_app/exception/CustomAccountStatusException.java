package com.dealkartbd.backend_app.exception;

import org.springframework.security.authentication.AccountStatusException;

public class CustomAccountStatusException extends AccountStatusException {

    public CustomAccountStatusException(String msg) {
        super(msg);
    }

    public CustomAccountStatusException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

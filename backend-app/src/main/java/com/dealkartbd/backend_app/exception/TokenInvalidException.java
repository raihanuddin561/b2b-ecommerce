package com.dealkartbd.backend_app.exception;

public class TokenInvalidException extends RuntimeException{
    public TokenInvalidException(String message){
        super(message);
    }
}
package com.vsdya.taskflow.auth.application;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User already exists: " + email);
    }
}
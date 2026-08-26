package com.ai.shrija.leave.agent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LeaveValidationException extends RuntimeException {

    public LeaveValidationException(String message) {
        super(message);
    }
}

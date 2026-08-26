package com.ai.shrija.leave.agent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientLeaveException extends RuntimeException {

    public InsufficientLeaveException(String employeeId, String leaveType, double requested, double available) {
        super(String.format(
                "Employee %s requested %.1f day(s) of %s leave but only has %.1f day(s) available",
                employeeId, requested, leaveType, available));
    }
}

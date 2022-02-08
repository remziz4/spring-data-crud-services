package com.tourneycompanion.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ServiceException extends RuntimeException {

    private final String message;
    private final int status;
    private final List<String> violations;

    public ServiceException(final String message, final int status){
        this.message = message;
        this.status = status;
        this.violations = new ArrayList<>();
    }

    public ServiceException(final String message, final int status, final List<String> violations){
        this.message = message;
        this.status = status;
        this.violations = violations;
    }
}

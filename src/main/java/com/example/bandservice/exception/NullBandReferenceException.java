package com.example.bandservice.exception;

public class NullBandReferenceException extends RuntimeException {

    public NullBandReferenceException() {
    }

    public NullBandReferenceException(String message) {
        super(message);
    }

}

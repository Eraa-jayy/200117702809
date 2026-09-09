package com.example.trainingmanagement.exception;

public class MaxParticipantsReachedException extends RuntimeException {

    public MaxParticipantsReachedException(String message) {
        super(message);
    }
}
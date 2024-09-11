package com.community.api.services.exception;

public class FileSizeExceededException extends Exception {
    public FileSizeExceededException(String message) {
        super(message);
    }
}

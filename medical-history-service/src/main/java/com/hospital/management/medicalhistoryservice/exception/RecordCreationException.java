package com.hospital.management.medicalhistoryservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecordCreationException extends RuntimeException{
    public RecordCreationException(String message){
        super(message);
    }
}

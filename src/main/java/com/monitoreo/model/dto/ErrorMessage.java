package com.monitoreo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    private LocalDateTime timeStamp;
    private String message;
    private int status;
    private String path;


    public ErrorMessage(String message) {
        this.message = message;
    }
}

package com.example.edulearn.ENTITY.Response;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ServerResponse {
    private String message ;
    private boolean status ;

    public ServerResponse() {
    }

    public ServerResponse(String message, boolean status) {
        this.message = message;
        this.status = status;
    }
}

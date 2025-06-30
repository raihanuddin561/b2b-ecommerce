package com.dealkartbd.backend_app.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    Object errorBody;
}

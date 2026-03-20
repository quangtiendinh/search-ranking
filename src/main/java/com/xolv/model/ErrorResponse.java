package com.xolv.model;

import java.time.LocalDateTime;

public record ErrorResponse(String message, int status, LocalDateTime timestamp) { }
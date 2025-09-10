package com.vat.authservice.dto;

import java.util.UUID;

public record ResponseDTO(String token, UUID userID) {
}

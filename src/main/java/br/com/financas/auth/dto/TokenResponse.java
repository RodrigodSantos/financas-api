package br.com.financas.auth.dto;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm) {
}

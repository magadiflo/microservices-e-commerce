package dev.magadiflo.ecommerce.app.handlers;

import java.util.Map;

public record ErrorResponse(Map<String, String> errors) {
}

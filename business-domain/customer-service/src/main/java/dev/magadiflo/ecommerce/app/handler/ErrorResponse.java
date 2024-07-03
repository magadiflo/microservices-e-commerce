package dev.magadiflo.ecommerce.app.handler;

import java.util.Map;

public record ErrorResponse(Map<String, String> errors) {
}

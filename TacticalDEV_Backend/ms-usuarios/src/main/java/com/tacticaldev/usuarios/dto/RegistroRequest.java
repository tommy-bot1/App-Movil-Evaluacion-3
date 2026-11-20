package com.tacticaldev.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z]+\\.com$", message = "Formato de correo inválido. Ejemplo: tomas@ventas.com")
        String correo,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "La clave es obligatoria")
        @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
        String clave) {
}

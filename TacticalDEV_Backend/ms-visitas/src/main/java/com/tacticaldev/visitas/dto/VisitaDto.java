package com.tacticaldev.visitas.dto;

import jakarta.validation.constraints.*;


public record VisitaDto(
        Long id,
        @NotBlank(message = "El correo del operario es obligatorio") String correo,
        @NotBlank(message = "El nombre del operario es obligatorio") String nombreOperario,
        @NotBlank(message = "El perfil del operario es obligatorio") String rolOperario,
        @NotBlank(message = "El código del activo es obligatorio")
        @Pattern(regexp = "^[A-Z]{3}-\\d{3,6}$", message = "Código de activo inválido. Ejemplo: VTR-1020")
        String codigoActivo,
        @NotBlank(message = "El tipo de activo es obligatorio")
        @Pattern(regexp = "Vitrina freezer|Conservadora", message = "Tipo de activo inválido")
        String tipoActivo,
        @NotBlank(message = "El punto de venta es obligatorio") String nombreTienda,
        @NotBlank(message = "El tipo de visita es obligatorio")
        @Pattern(regexp = "Mantenimiento Preventivo|Inspección", message = "Tipo de visita inválido")
        String tipoVisita,
        @NotBlank(message = "El estado del equipo es obligatorio")
        @Pattern(regexp = "Bueno|Requiere Mantención|Falla Crítica", message = "Estado del equipo inválido")
        String estadoEquipo,
        @NotBlank(message = "Debe agregar una observación técnica")
        @Size(max = 1000, message = "La observación no puede superar 1000 caracteres")
        String observaciones,
        @Size(max = 500, message = "Ruta de foto demasiado larga") String rutaFoto,
        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90", message = "Latitud fuera de rango")
        @DecimalMax(value = "90", message = "Latitud fuera de rango")
        Double latitud,
        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180", message = "Longitud fuera de rango")
        @DecimalMax(value = "180", message = "Longitud fuera de rango")
        Double longitud,
        Float precisionGps,
        Float distanciaPuntoVentaMetros,
        @NotNull(message = "Debe indicar si la presencia fue verificada") Boolean presenciaVerificada,
        Long marcaTiempo) {
}

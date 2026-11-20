package com.tacticaldev.usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puntos_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90", message = "Latitud fuera de rango")
    @DecimalMax(value = "90", message = "Latitud fuera de rango")
    @Column(nullable = false)
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180", message = "Longitud fuera de rango")
    @DecimalMax(value = "180", message = "Longitud fuera de rango")
    @Column(nullable = false)
    private Double longitud;
}

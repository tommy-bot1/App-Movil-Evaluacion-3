package com.tacticaldev.visitas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visitas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(nullable = false, length = 80)
    private String nombreOperario;

    @Column(nullable = false, length = 20)
    private String rolOperario;

    @Column(nullable = false, length = 20)
    private String codigoActivo;

    @Column(nullable = false, length = 30)
    private String tipoActivo;

    @Column(nullable = false, length = 100)
    private String nombreTienda;

    @Column(nullable = false, length = 40)
    private String tipoVisita;

    @Column(nullable = false, length = 30)
    private String estadoEquipo;

    @Column(nullable = false, length = 1000)
    private String observaciones;

    @Column(length = 500)
    private String rutaFoto;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    private Float precisionGps;

    private Float distanciaPuntoVentaMetros;

    @Column(nullable = false)
    private Boolean presenciaVerificada;

    @Column(nullable = false)
    private Long marcaTiempo;
}

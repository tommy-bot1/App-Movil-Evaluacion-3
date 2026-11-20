package com.tacticaldev.usuarios.config;

import com.tacticaldev.usuarios.model.PuntoVenta;
import com.tacticaldev.usuarios.model.Usuario;
import com.tacticaldev.usuarios.repository.PuntoVentaRepository;
import com.tacticaldev.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class DatosIniciales implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PuntoVentaRepository puntos;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository usuarios, PuntoVentaRepository puntos, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.puntos = puntos;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0) {
            String clave = encoder.encode("123456");
            usuarios.save(new Usuario(null, "tomas@ventas.com", clave, "Tomas", "Vendedor"));
            usuarios.save(new Usuario(null, "gustavo@marketing.com", clave, "Gustavo", "Marketing"));
            usuarios.save(new Usuario(null, "jesus@transporte.com", clave, "jesus", "Transportista"));
        }
        if (puntos.count() == 0) {
            // Mismas coordenadas de demostración que tenía la app en Geocerca.kt; reemplazar por las reales.
            puntos.save(new PuntoVenta(null, "Tienda Central", -33.4489, -70.6693));
            puntos.save(new PuntoVenta(null, "Tienda Mall Plaza Norte", -33.3668, -70.6814));
            puntos.save(new PuntoVenta(null, "Tienda Costanera Center", -33.4170, -70.6062));
            puntos.save(new PuntoVenta(null, "Tienda Marina Arauco", -33.0205, -71.5518));
            puntos.save(new PuntoVenta(null, "Tienda Concepción Centro", -36.8269, -73.0498));
        }
    }
}

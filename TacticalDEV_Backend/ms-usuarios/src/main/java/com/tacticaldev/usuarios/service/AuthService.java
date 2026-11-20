package com.tacticaldev.usuarios.service;

import com.tacticaldev.usuarios.dto.LoginRequest;
import com.tacticaldev.usuarios.dto.RegistroRequest;
import com.tacticaldev.usuarios.dto.UsuarioRespuesta;
import com.tacticaldev.usuarios.exception.ApiException;
import com.tacticaldev.usuarios.model.Usuario;
import com.tacticaldev.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public static String rolDesdeCorreo(String correo) {
        String dominio = correo.substring(correo.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (!dominio.endsWith(".com")) return null;
        return switch (dominio.substring(0, dominio.length() - 4)) {
            case "ventas", "vendedor" -> "Vendedor";
            case "tecnico" -> "Técnico";
            case "transporte", "transportista" -> "Transportista";
            default -> null;
        };
    }

    public UsuarioRespuesta login(LoginRequest req) {
        Usuario u = repo.findByCorreo(req.correo().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        if (!encoder.matches(req.clave(), u.getClaveHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return UsuarioRespuesta.de(u);
    }

    public UsuarioRespuesta registrar(RegistroRequest req) {
        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        String rol = rolDesdeCorreo(correo);
        if (rol == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Dominio no reconocido. Usa @ventas.com, @tecnico.com o @transporte.com");
        }
        if (repo.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        Usuario u = repo.save(new Usuario(null, correo, encoder.encode(req.clave()), req.nombre().trim(), rol));
        return UsuarioRespuesta.de(u);
    }
}
